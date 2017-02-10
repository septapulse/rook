package run.rook.test.transport;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import run.rook.api.RID;
import run.rook.api.transport.ControllableTransport;
import run.rook.api.transport.GrowableBuffer;
import run.rook.api.transport.Transport;
import run.rook.api.transport.simple.MessageType;

public abstract class AbstractTransportTest {

	protected RID id1 = RID.create("S1");
	protected RID id2 = RID.create("S2");
	protected RID id3 = RID.create("S3");
	protected RID groupA = RID.create("A");
	protected RID groupB = RID.create("B");
	protected ControllableTransport t1;
	protected ControllableTransport t2;
	protected ControllableTransport t3;
	protected BlockingQueue<Message> q1;
	protected BlockingQueue<Message> q2;
	protected BlockingQueue<Message> q3;
	
	@Before
	public void startTransports() throws Exception {
		beforeStartup();
		t1 = createTransport1();
		t2 = createTransport2();
		t3 = createTransport3();
		t1.setServiceId(id1);
		t2.setServiceId(id2);
		t3.setServiceId(id3);
		t1.start();
		t2.start();
		t3.start();
		Thread.sleep(100);
		q1 = new LinkedBlockingQueue<>();
		q2 = new LinkedBlockingQueue<>();
		q3 = new LinkedBlockingQueue<>();
		listen(t1, q1);
		listen(t2, q2);
		listen(t3, q3);
		afterStartup();
	}
	
	protected abstract void beforeStartup() throws Exception;
	protected abstract void afterStartup() throws Exception;
	protected abstract ControllableTransport createTransport1() throws Exception;
	protected abstract ControllableTransport createTransport2() throws Exception;
	protected abstract ControllableTransport createTransport3() throws Exception;

	@After
	public void stopTransports() throws Exception {
		try {
			beforeTeardown();
			if(t1 != null) t1.shutdown();
			if(t2 != null) t2.shutdown();
			if(t3 != null) t3.shutdown();
			t1 = null;
			t2 = null;
			t3 = null;
		} finally {
			afterTeardown();
		}
	}
	
	protected abstract void beforeTeardown() throws Exception;
	protected abstract void afterTeardown() throws Exception;
	
	@Test
	public void testAnnounce() throws Exception {
		t1.announce().addAnnouncementConsumer((RID id) -> q1.add(new Message().type(MessageType.ANNOUNCE).from(id)));

		Thread.sleep(1000);

		t1.announce().probe();
		
		Thread.sleep(1000);
		
		Set<RID> expected = new HashSet<>();
		expected.add(id1);
		expected.add(id2);
		expected.add(id3);

		assertAnnouncements(q1, expected);
		
		Assert.assertEquals("Not empty: " + q1, 0, q1.size());
		Assert.assertEquals("Not empty: " + q2, 0, q2.size());
		Assert.assertEquals("Not empty: " + q3, 0, q3.size());
	}
	
	private void assertAnnouncements(BlockingQueue<Message> q, Set<RID> expected) throws InterruptedException {
		Set<RID> actual = new LinkedHashSet<>();
		for(int i = 0; i < expected.size(); i++) {
			Message m = q.poll(1, TimeUnit.SECONDS);
			if(m == null)
				Assert.fail("Did not receive enough announcements. Expected="+expected.size() + " Actual=" + i);
			if(m.type != MessageType.ANNOUNCE)
				Assert.fail("Expected ANNOUNCEMENT but was " + m.type);
			actual.add(m.from);
		}
		for(RID exp : expected) {
			Assert.assertTrue(expected + " != " + actual, actual.contains(exp));
		}
	}

	@Test
	public void testUnicast() throws Exception {
		GrowableBuffer payload = GrowableBuffer.copyFrom(new byte[] { 0, 1, 2, 3, 4, 5 });
		
		t2.ucast().send(id1, payload);
		Assert.assertEquals(new Message().type(MessageType.UCAST_MESSAGE).from(id2).to(id1).buf(payload), q1.poll(1, TimeUnit.SECONDS));
		Assert.assertEquals("Not empty: " + q1, 0, q1.size());
		Assert.assertEquals("Not empty: " + q2, 0, q2.size());
		Assert.assertEquals("Not empty: " + q3, 0, q3.size());
		
		t1.ucast().send(id2, payload);
		Assert.assertEquals(new Message().type(MessageType.UCAST_MESSAGE).from(id1).to(id2).buf(payload), q2.poll(1, TimeUnit.SECONDS));
		Assert.assertEquals("Not empty: " + q1, 0, q1.size());
		Assert.assertEquals("Not empty: " + q2, 0, q2.size());
		Assert.assertEquals("Not empty: " + q3, 0, q3.size());
		
		t2.ucast().send(id3, payload);
		Assert.assertEquals(new Message().type(MessageType.UCAST_MESSAGE).from(id2).to(id3).buf(payload), q3.poll(1, TimeUnit.SECONDS));
		Assert.assertEquals("Not empty: " + q1, 0, q1.size());
		Assert.assertEquals("Not empty: " + q2, 0, q2.size());
		Assert.assertEquals("Not empty: " + q3, 0, q3.size());
	}
	
	@Test
	public void testUnicastIncognitoConsume() throws Exception {
		t3.ucast().incognito_addMessageConsumer((RID from, RID to, GrowableBuffer buf) -> q3.add(new Message().type(MessageType.UCAST_MESSAGE).from(from).to(to).buf(buf)));
		GrowableBuffer payload = GrowableBuffer.copyFrom(new byte[] { 0, 1, 2, 3, 4, 5 });
		t2.ucast().send(id1, payload);
		// check service the message was to
		Assert.assertEquals(new Message().type(MessageType.UCAST_MESSAGE).from(id2).to(id1).buf(payload), q1.poll(1, TimeUnit.SECONDS));
		// check service that was incognito
		Assert.assertEquals(new Message().type(MessageType.UCAST_MESSAGE).from(id2).to(id1).buf(payload), q3.poll(1, TimeUnit.SECONDS));
		Assert.assertEquals("Not empty: " + q1, 0, q1.size());
		Assert.assertEquals("Not empty: " + q2, 0, q2.size());
		Assert.assertEquals("Not empty: " + q3, 0, q3.size());
	}
	
	@Test
	public void testUnicastIncognitoProduce() throws Exception {
		GrowableBuffer payload = GrowableBuffer.copyFrom(new byte[] { 0, 1, 2, 3, 4, 5 });
		
		t1.ucast().incognito_send(id2, id3, payload);
		// check service the message was to
		Assert.assertEquals(new Message().type(MessageType.UCAST_MESSAGE).from(id2).to(id3).buf(payload), q3.poll(1, TimeUnit.SECONDS));
		Assert.assertEquals("Not empty: " + q1, 0, q1.size());
		Assert.assertEquals("Not empty: " + q2, 0, q2.size());
		Assert.assertEquals("Not empty: " + q3, 0, q3.size());
		
		t2.ucast().incognito_send(id1, id3, payload);
		// check service the message was to
		Assert.assertEquals(new Message().type(MessageType.UCAST_MESSAGE).from(id1).to(id3).buf(payload), q3.poll(1, TimeUnit.SECONDS));
		Assert.assertEquals("Not empty: " + q1, 0, q1.size());
		Assert.assertEquals("Not empty: " + q2, 0, q2.size());
		Assert.assertEquals("Not empty: " + q3, 0, q3.size());
	}
	
	@Test
	public void testBroadcast() throws Exception {
		GrowableBuffer payloadA = GrowableBuffer.copyFrom(new byte[] { 0, 1 });
		GrowableBuffer payloadB = GrowableBuffer.copyFrom(new byte[] { 2, 3 });
		
		// T1 joins GROUP A
		t1.bcast().join(groupA);
		Assert.assertEquals(new Message().type(MessageType.BCAST_JOIN).from(id1).group(groupA), q1.poll(1, TimeUnit.SECONDS));
		Assert.assertEquals(new Message().type(MessageType.BCAST_JOIN).from(id1).group(groupA), q2.poll(1, TimeUnit.SECONDS));
		Assert.assertEquals(new Message().type(MessageType.BCAST_JOIN).from(id1).group(groupA), q3.poll(1, TimeUnit.SECONDS));
		Thread.sleep(200);
		Assert.assertEquals("Not empty: " + q1, 0, q1.size());
		Assert.assertEquals("Not empty: " + q2, 0, q2.size());
		Assert.assertEquals("Not empty: " + q3, 0, q3.size());
		
		// T2 joins GROUP A
		t2.bcast().join(groupA);
		Assert.assertEquals(new Message().type(MessageType.BCAST_JOIN).from(id2).group(groupA), q1.poll(1, TimeUnit.SECONDS));
		Assert.assertEquals(new Message().type(MessageType.BCAST_JOIN).from(id2).group(groupA), q2.poll(1, TimeUnit.SECONDS));
		Assert.assertEquals(new Message().type(MessageType.BCAST_JOIN).from(id2).group(groupA), q3.poll(1, TimeUnit.SECONDS));
		Thread.sleep(200);
		Assert.assertEquals("Not empty: " + q1, 0, q1.size());
		Assert.assertEquals("Not empty: " + q2, 0, q2.size());
		Assert.assertEquals("Not empty: " + q3, 0, q3.size());
		
		// T3 joins GROUP B 
		t3.bcast().join(groupB);
		Assert.assertEquals(new Message().type(MessageType.BCAST_JOIN).from(id3).group(groupB), q1.poll(1, TimeUnit.SECONDS));
		Assert.assertEquals(new Message().type(MessageType.BCAST_JOIN).from(id3).group(groupB), q2.poll(1, TimeUnit.SECONDS));
		Assert.assertEquals(new Message().type(MessageType.BCAST_JOIN).from(id3).group(groupB), q3.poll(1, TimeUnit.SECONDS));
		Thread.sleep(200);
		Assert.assertEquals("Not empty: " + q1, 0, q1.size());
		Assert.assertEquals("Not empty: " + q2, 0, q2.size());
		Assert.assertEquals("Not empty: " + q3, 0, q3.size());
		
		// T1 sends GROUP A (to T1 and T2)
		t1.bcast().send(groupA, payloadA);
		Assert.assertEquals(new Message().type(MessageType.BCAST_MESSAGE).from(id1).group(groupA).buf(payloadA), q1.poll(1, TimeUnit.SECONDS));
		Assert.assertEquals(new Message().type(MessageType.BCAST_MESSAGE).from(id1).group(groupA).buf(payloadA), q2.poll(2, TimeUnit.SECONDS));
		Thread.sleep(200);
		Assert.assertEquals("Not empty: " + q1, 0, q1.size());
		Assert.assertEquals("Not empty: " + q2, 0, q2.size());
		Assert.assertEquals("Not empty: " + q3, 0, q3.size());
		
		// T1 sends GROUP B (to T3)
		t1.bcast().send(groupB, payloadB);
		Assert.assertEquals(new Message().type(MessageType.BCAST_MESSAGE).from(id1).group(groupB).buf(payloadB), q3.poll(1, TimeUnit.SECONDS));
		Thread.sleep(200);
		Assert.assertEquals("Not empty: " + q1, 0, q1.size());
		Assert.assertEquals("Not empty: " + q2, 0, q2.size());
		Assert.assertEquals("Not empty: " + q3, 0, q3.size());
		
		// T2 leaves GROUP A
		t2.bcast().leave(groupA);
		Assert.assertEquals(new Message().type(MessageType.BCAST_LEAVE).from(id2).group(groupA), q1.poll(1, TimeUnit.SECONDS));
		Assert.assertEquals(new Message().type(MessageType.BCAST_LEAVE).from(id2).group(groupA), q2.poll(1, TimeUnit.SECONDS));
		Assert.assertEquals(new Message().type(MessageType.BCAST_LEAVE).from(id2).group(groupA), q3.poll(1, TimeUnit.SECONDS));
		Thread.sleep(200);
		Assert.assertEquals("Not empty: " + q1, 0, q1.size());
		Assert.assertEquals("Not empty: " + q2, 0, q2.size());
		Assert.assertEquals("Not empty: " + q3, 0, q3.size());
		
		// T1 sends GROUP A (to T1)
		t1.bcast().send(groupA, payloadA);
		Assert.assertEquals(new Message().type(MessageType.BCAST_MESSAGE).from(id1).group(groupA).buf(payloadA), q1.poll(1, TimeUnit.SECONDS));
		Thread.sleep(200);
		Assert.assertEquals("Not empty: " + q1, 0, q1.size());
		Assert.assertEquals("Not empty: " + q2, 0, q2.size());
		Assert.assertEquals("Not empty: " + q3, 0, q3.size());
		
		// T2 leaves GROUP A
		t1.bcast().leave(groupA);
		Assert.assertEquals(new Message().type(MessageType.BCAST_LEAVE).from(id1).group(groupA), q1.poll(1, TimeUnit.SECONDS));
		Assert.assertEquals(new Message().type(MessageType.BCAST_LEAVE).from(id1).group(groupA), q2.poll(1, TimeUnit.SECONDS));
		Assert.assertEquals(new Message().type(MessageType.BCAST_LEAVE).from(id1).group(groupA), q3.poll(1, TimeUnit.SECONDS));
		Thread.sleep(200);
		Assert.assertEquals("Not empty: " + q1, 0, q1.size());
		Assert.assertEquals("Not empty: " + q2, 0, q2.size());
		Assert.assertEquals("Not empty: " + q3, 0, q3.size());
		
		// T1 sends GROUP A (to nobody)
		t1.bcast().send(groupA, payloadA);
		Thread.sleep(200);
		Assert.assertEquals("Not empty: " + q1, 0, q1.size());
		Assert.assertEquals("Not empty: " + q2, 0, q2.size());
		Assert.assertEquals("Not empty: " + q3, 0, q3.size());
		
		// T3 leaves GROUP B
		t3.bcast().leave(groupB);
		Assert.assertEquals(new Message().type(MessageType.BCAST_LEAVE).from(id3).group(groupB), q1.poll(1, TimeUnit.SECONDS));
		Assert.assertEquals(new Message().type(MessageType.BCAST_LEAVE).from(id3).group(groupB), q2.poll(1, TimeUnit.SECONDS));
		Assert.assertEquals(new Message().type(MessageType.BCAST_LEAVE).from(id3).group(groupB), q3.poll(1, TimeUnit.SECONDS));
		Thread.sleep(200);
		Assert.assertEquals("Not empty: " + q1, 0, q1.size());
		Assert.assertEquals("Not empty: " + q2, 0, q2.size());
		Assert.assertEquals("Not empty: " + q3, 0, q3.size());
		
		// T1 sends GROUP B (to nobody)
		t1.bcast().send(groupB, payloadB);
		Thread.sleep(200);
		Assert.assertEquals("Not empty: " + q1, 0, q1.size());
		Assert.assertEquals("Not empty: " + q2, 0, q2.size());
		Assert.assertEquals("Not empty: " + q3, 0, q3.size());
	}

	private void listen(Transport t, BlockingQueue<Message> q) {
		t.ucast().addMessageConsumer((RID from, RID to, GrowableBuffer buf) -> q.add(new Message().type(MessageType.UCAST_MESSAGE).from(from).to(to).buf(buf)));
		t.bcast().addJoinConsumer((RID from, RID group) -> q.add(new Message().type(MessageType.BCAST_JOIN).from(from).group(group)));
		t.bcast().addLeaveConsumer((RID from, RID group) -> q.add(new Message().type(MessageType.BCAST_LEAVE).from(from).group(group)));
		t.bcast().addMessageConsumer((RID f, RID g, GrowableBuffer b) -> q.add(new Message().type(MessageType.BCAST_MESSAGE).from(f).group(g).buf(b)));
	}
	
	private static class Message {
		MessageType type;
		RID from;
		RID to;
		RID group;
		GrowableBuffer buf;
		public Message type(MessageType type) {
			this.type = type;
			return this;
		}
		public Message from(RID from) {
			this.from = from.copy();
			return this;
		}
		public Message to(RID to) {
			this.to = to.copy();
			return this;
		}
		public Message group(RID group) {
			this.group = group.copy();
			return this;
		}
		public Message buf(GrowableBuffer buf) {
			this.buf = buf.copy();
			return this;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((buf == null) ? 0 : buf.hashCode());
			result = prime * result + ((from == null) ? 0 : from.hashCode());
			result = prime * result + ((group == null) ? 0 : group.hashCode());
			result = prime * result + ((to == null) ? 0 : to.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Message other = (Message) obj;
			if (buf == null) {
				if (other.buf != null)
					return false;
			} else if (!buf.equals(other.buf))
				return false;
			if (from == null) {
				if (other.from != null)
					return false;
			} else if (!from.equals(other.from))
				return false;
			if (group == null) {
				if (other.group != null)
					return false;
			} else if (!group.equals(other.group))
				return false;
			if (to == null) {
				if (other.to != null)
					return false;
			} else if (!to.equals(other.to))
				return false;
			if (type != other.type)
				return false;
			return true;
		}
		@Override
		public String toString() {
			return "Message [type=" + type + ", from=" + from + ", to=" + to + ", group=" + group + ", buf=" + buf
					+ "]";
		}
	}
	
}
