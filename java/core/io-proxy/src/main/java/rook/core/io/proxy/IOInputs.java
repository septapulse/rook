package rook.core.io.proxy;

import rook.api.Service;
import rook.api.transport.Transport;
import rook.core.io.proxy.message.CapType;

/**
 * Listens to Inputs from all IO {@link Service}s
 * 
 * @author Eric Thill
 *
 */
public class IOInputs extends IOListener {

	IOInputs(Transport transport) {
		super(transport, IOGroups.INPUT, CapType.INPUT);
	}
	
}
