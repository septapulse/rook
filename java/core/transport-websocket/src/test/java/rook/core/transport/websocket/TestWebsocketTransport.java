package rook.core.transport.websocket;

import rook.api.transport.AbstractTransportTest;
import rook.api.transport.ControllableTransport;

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
