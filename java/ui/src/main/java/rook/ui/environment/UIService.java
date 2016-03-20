package rook.ui.environment;

import rook.api.InitException;
import rook.api.Service;
import rook.api.transport.Transport;
import rook.core.io.proxy.IOProxy;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
class UIService implements Service {
	private Transport transport;
	private IOProxy ioProxy;
	
	public IOProxy getIOProxy() {
		return ioProxy;
	} 
	
	@Override
	public void setTransport(Transport transport) {
		this.transport = transport;
	}

	@Override
	public void init() throws InitException {
		ioProxy = new IOProxy(transport);
	}

	@Override
	public void shutdown() {

	}
};