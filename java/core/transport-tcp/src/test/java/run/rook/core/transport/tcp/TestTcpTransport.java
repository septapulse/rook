package run.rook.core.transport.tcp;

import run.rook.api.transport.ControllableTransport;
import run.rook.core.transport.tcp.TcpRouter;
import run.rook.core.transport.tcp.TcpRouterConfig;
import run.rook.core.transport.tcp.TcpTransport;
import run.rook.core.transport.tcp.TcpTransportConfig;
import run.rook.test.transport.AbstractTransportTest;

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
