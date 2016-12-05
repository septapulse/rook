package io.septapulse.rook.core.io.proxy;

import io.septapulse.rook.api.Service;
import io.septapulse.rook.api.transport.Transport;
import io.septapulse.rook.core.io.proxy.message.CapType;

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
