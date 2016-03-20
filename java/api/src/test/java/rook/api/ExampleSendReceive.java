package rook.api;

import java.util.function.Consumer;

import rook.api.config.ConfigurableConstructor;
import rook.api.transport.GrowableBuffer;
import rook.api.transport.Transport;
import rook.api.transport.event.UnicastMessage;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

public class ExampleSendReceive {

	
	public static void main(String[] args) throws Exception {
		RookRunner.main(new String[] { "src/test/resources/senderReceiverTest.json" });
	}
	
	public static class ReceiverService implements Service {
		
		private Transport transport;
		
		@Override
		public void shutdown() {
			
		}
		
		@Override
		public void setTransport(Transport transport) {
			this.transport = transport;
		}
		
		@Override
		public void init() {
			transport.ucast().addMessageConsumer(new Consumer<UnicastMessage<GrowableBuffer>>() {
				public void accept(rook.api.transport.event.UnicastMessage<GrowableBuffer> t) {
					System.out.println("From: " + t.getFrom() + " Msg: " + t.getPayload());
				}
			});
		}
	};
	
	public static class SenderService implements Service {
		
		private Transport transport;
		private final RID receiverID;
		
		@ConfigurableConstructor
		public SenderService(SenderConfig cfg) {
			receiverID = RID.create(cfg.receiverID);
		}

		@Override
		public void setTransport(Transport transport) {
			this.transport = transport;
		}
		
		@Override
		public void init() {
			new Thread(loop).start();
		}
		
		private final Runnable loop = new Runnable() {
			
			@Override
			public void run() {
				for(int i = 0; i < 512; i++) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					GrowableBuffer msg = GrowableBuffer.allocate(4);
					UnsafeBuffer buf = new UnsafeBuffer(msg.getBytes());
					buf.putInt(0, i);
					msg.setLength(4);
					transport.ucast().send(receiverID, msg);
				}
			}
		};
		
		@Override
		public void shutdown() {
			
		}
		
		public static class SenderConfig {
			public String receiverID;
		}
	}; 
}
