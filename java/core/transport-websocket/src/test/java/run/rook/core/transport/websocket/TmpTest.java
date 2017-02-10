package run.rook.core.transport.websocket;

import run.rook.api.RID;
import run.rook.api.exception.ExceptionHandler;
import run.rook.core.transport.websocket.WebsocketRouter;
import run.rook.core.transport.websocket.WebsocketRouterConfig;
import run.rook.core.transport.websocket.WebsocketTransport;
import run.rook.core.transport.websocket.WebsocketTransportConfig;

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
