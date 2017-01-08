package io.septapulse.rook.core.io.proxy;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.Service;
import io.septapulse.rook.api.transport.Transport;

/**
 * Listens to Inputs from all IO {@link Service}s
 * 
 * @author Eric Thill
 *
 */
public class IOInputs extends IOListener {

	IOInputs(Transport transport, RID ioServiceId) {
		super(transport, ioServiceId, IOGroups.INPUT);
	}
	
}
