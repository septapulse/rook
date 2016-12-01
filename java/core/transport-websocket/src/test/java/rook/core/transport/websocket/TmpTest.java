package rook.core.transport.websocket;

import rook.api.RID;
import rook.api.exception.ExceptionHandler;

public class TmpTest {

	public static void main(String[] args) throws Exception {
		WebsocketRouter router = new WebsocketRouter(new WebsocketRouterConfig());
		router.start();
		
		WebsocketTransport transport = new WebsocketTransport(new WebsocketTransportConfig());
		transport.setServiceId(RID.create("T1"));
		transport.setExceptionHandler(new ExceptionHandler() {
			@Override
			public void error(String message, Throwable t) {
				System.err.println(message);
				if(t != null) {
					t.printStackTrace();
				}
			}
		});
		transport.start();
		transport.announce().addAnnouncementConsumer(System.out::println);

		transport.announce().probe();
		
		Thread.sleep(3000);
		
		router.stop();
	}
}
