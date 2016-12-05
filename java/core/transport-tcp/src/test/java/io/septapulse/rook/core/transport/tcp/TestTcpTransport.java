package io.septapulse.rook.core.transport.tcp;

import io.septapulse.rook.api.transport.AbstractTransportTest;
import io.septapulse.rook.api.transport.ControllableTransport;
import io.septapulse.rook.core.transport.tcp.TcpRouter;
import io.septapulse.rook.core.transport.tcp.TcpRouterConfig;
import io.septapulse.rook.core.transport.tcp.TcpTransport;
import io.septapulse.rook.core.transport.tcp.TcpTransportConfig;

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
