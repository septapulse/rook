package rook.api;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import rook.api.router.disruptor.DisruptorRouter;
import rook.api.transport.GrowableBuffer;
import rook.api.transport.StringDeserializer;
import rook.api.transport.event.BroadcastMessage;
import rook.api.transport.event.UnicastMessage;

public class TestDisruptorRouter {
	
	private static RID ID1 = RID.create("SERVICE1").unmodifiable();
	private static RID ID2 = RID.create("SERVICE2").unmodifiable();
	private static RID ID3 = RID.create("SERVICE3").unmodifiable();
	
	DisruptorRouter router;
	QueueService<String> service1;
	QueueService<String> service2;
	QueueService<String> service3;
	
	@Before
	public void startupRouter() throws InitException {
		router = new DisruptorRouter();
		service1 = new QueueService<>(new StringDeserializer());
		service2 = new QueueService<>(new StringDeserializer());
		service3 = new QueueService<>(new StringDeserializer());
		router.addService(ID1, service1);
		router.addService(ID2, service2);
		router.addService(ID3, service3);
		router.start();
	}
	
	@After
	public void clearQueues() throws InterruptedException {
		router.shutdown();
		Thread.sleep(100);
		service1.reset();
		service2.reset();
		service3.reset();
	}
	
	private void assertAllQueuesEmpty() {
		Assert.assertEquals(0, service1.getBcastQueue().size());
		Assert.assertEquals(0, service2.getBcastQueue().size());
		Assert.assertEquals(0, service3.getBcastQueue().size());
		Assert.assertEquals(0, service1.getUcastQueue().size());
		Assert.assertEquals(0, service2.getUcastQueue().size());
		Assert.assertEquals(0, service3.getUcastQueue().size());
		Assert.assertEquals(0, service1.getUcastTapQueue().size());
		Assert.assertEquals(0, service2.getUcastTapQueue().size());
		Assert.assertEquals(0, service3.getUcastTapQueue().size());
	}
	
	@Test
	public void testUnicast() {
		// test send to some service
		service1.getTransport().ucast().send(ID2, toBuf("TEST1"));
		Assert.assertEquals("TEST1", poll_ucast(service2.getUcastQueue()).getPayload());
		Assert.assertEquals("TEST1", poll_ucast(service1.getUcastTapQueue()).getPayload());
		Assert.assertEquals("TEST1", poll_ucast(service2.getUcastTapQueue()).getPayload());
		Assert.assertEquals("TEST1", poll_ucast(service3.getUcastTapQueue()).getPayload());
		assertAllQueuesEmpty();
		
		// test send to a different service
		service1.getTransport().ucast().send(ID3, toBuf("TEST2"));
		Assert.assertEquals("TEST2", poll_ucast(service3.getUcastQueue()).getPayload());
		Assert.assertEquals("TEST2", poll_ucast(service1.getUcastTapQueue()).getPayload());
		Assert.assertEquals("TEST2", poll_ucast(service2.getUcastTapQueue()).getPayload());
		Assert.assertEquals("TEST2", poll_ucast(service3.getUcastTapQueue()).getPayload());
		assertAllQueuesEmpty();
		
		// test send to self
		service1.getTransport().ucast().send(ID1, toBuf("TEST3"));
		Assert.assertEquals("TEST3", poll_ucast(service1.getUcastQueue()).getPayload());
		Assert.assertEquals("TEST3", poll_ucast(service1.getUcastTapQueue()).getPayload());
		Assert.assertEquals("TEST3", poll_ucast(service2.getUcastTapQueue()).getPayload());
		Assert.assertEquals("TEST3", poll_ucast(service3.getUcastTapQueue()).getPayload());
		assertAllQueuesEmpty();
	}
	
	private GrowableBuffer toBuf(String string) {
		byte[] bytes = string.getBytes();
		GrowableBuffer buf = GrowableBuffer.allocate(bytes.length);
		buf.put(bytes, 0, bytes.length);
		return buf;
	}

	@Test
	public void testBroadcastJoin() {
		RID grp = RID.create("GRP.TEST1");
		
		// join
		service2.bcastJoin(grp);
		
		// test send to other service
		service1.getTransport().bcast().send(grp, toBuf("HELLO"));
		Assert.assertEquals("HELLO", poll_bcast(service2.getBcastQueue()).getPayload());
		assertAllQueuesEmpty();
		
		// test send to self
		service2.getTransport().bcast().send(grp, toBuf("TEST"));
		Assert.assertEquals("TEST", poll_bcast(service2.getBcastQueue()).getPayload());
		assertAllQueuesEmpty();
	}
	
	@Test
	public void testBroadcastLeave() {
		RID grp = RID.create("GRP.TEST1");
		
		// join
		service2.bcastJoin(grp);
		
		// test receive while joined
		service1.getTransport().bcast().send(grp, toBuf("HELLO"));
		Assert.assertEquals("HELLO", poll_bcast(service2.getBcastQueue()).getPayload());
		assertAllQueuesEmpty();
		
		// leave
		service2.bcastLeave(grp);
		
		// test messages are no longer received
		service1.getTransport().bcast().send(grp, toBuf("TEST"));
		service2.getTransport().bcast().send(grp, toBuf("TEST"));
		assertAllQueuesEmpty();
	}
	
	private BroadcastMessage<String> poll_bcast(BlockingQueue<BroadcastMessage<String>> queue) {
		try {
			BroadcastMessage<String> m = queue.poll(1, TimeUnit.SECONDS);
			if(m == null) {
				Assert.fail("Queue is empty");
			}
			return m;
		} catch (InterruptedException e) {
			throw new IllegalStateException();
		}
	}
	
	private UnicastMessage<String> poll_ucast(BlockingQueue<UnicastMessage<String>> queue) {
		try {
			UnicastMessage<String> m = queue.poll(1, TimeUnit.SECONDS);
			if(m == null) {
				Assert.fail("Queue is empty");
			}
			return m;
		} catch (InterruptedException e) {
			throw new IllegalStateException();
		}
	}
}
