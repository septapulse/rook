package run.rook.core.transport.websocket;

import run.rook.api.transport.ControllableTransport;
import run.rook.core.transport.websocket.WebsocketRouter;
import run.rook.core.transport.websocket.WebsocketRouterConfig;
import run.rook.core.transport.websocket.WebsocketTransport;
import run.rook.core.transport.websocket.WebsocketTransportConfig;
import run.rook.test.transport.AbstractTransportTest;

public class TestWebsocketTransport extends AbstractTransportTest {

	private WebsocketRouter router;
	
	@Override
	protected void beforeStartup() throws Exception {
		if(router != null) {
			router.stop();
			router = null;
		}
		router = new WebsocketRouter(new WebsocketRouterConfig());
		router.start();
	}
	
	@Override
	protected void afterStartup() throws Exception {
		
	}
	
	@Override
	protected ControllableTransport createTransport1() throws Exception {
		return new WebsocketTransport(new WebsocketTransportConfig());
	}
	
	@Override
	protected ControllableTransport createTransport2() throws Exception {
		return new WebsocketTransport(new WebsocketTransportConfig());
	}
	
	@Override
	protected ControllableTransport createTransport3() throws Exception {
		return new WebsocketTransport(new WebsocketTransportConfig());
	}
	
	@Override
	protected void beforeTeardown() {
		
	}
	
	@Override
	protected void afterTeardown() throws Exception {
		if(router != null) {
			router.stop();
			router = null;
		}
	}

}
