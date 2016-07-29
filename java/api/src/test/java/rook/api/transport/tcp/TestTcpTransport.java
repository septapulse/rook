package rook.api.transport.tcp;

import rook.api.transport.AbstractTransportTest;
import rook.api.transport.ControllableTransport;

public class TestTcpTransport extends AbstractTransportTest {

	private TcpRouter router;
	
	@Override
	protected void beforeStartup() throws Exception {
		router = new TcpRouter(new TcpRouterConfig());
		router.start();
	}
	
	@Override
	protected void afterStartup() throws Exception {
		if(router != null) {
			router.stop();
			router = null;
		}
	}
	
	@Override
	protected ControllableTransport createTransport1() throws Exception {
		return new TcpTransport(new TcpTransportConfig());
	}
	
	@Override
	protected ControllableTransport createTransport2() throws Exception {
		return new TcpTransport(new TcpTransportConfig());
	}
	
	@Override
	protected ControllableTransport createTransport3() throws Exception {
		return new TcpTransport(new TcpTransportConfig());
	}
	
	@Override
	protected void beforeTeardown() {
		
	}
	
	@Override
	protected void afterTeardown() throws Exception {
		// time for server to close OS-side
		Thread.sleep(100);
	}

}
