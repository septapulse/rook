package run.rook.core.io.proxy;

import run.rook.api.RID;
import run.rook.api.Service;
import run.rook.api.transport.Transport;

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
