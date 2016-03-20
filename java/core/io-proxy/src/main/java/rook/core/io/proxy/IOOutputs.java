package rook.core.io.proxy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rook.api.RID;
import rook.api.Service;
import rook.api.transport.Transport;
import rook.core.io.proxy.message.CapType;
import rook.core.io.proxy.message.IOValue;
import rook.core.io.proxy.message.IOValueSerializer;
import rook.core.io.proxy.message.IOValuesSerializer;

/**
 * Listens to outputs from all IO {@link Service}s. Serializes and sends
 * {@link IOValue}s to the appropriate IO {@link Service} based on the
 * output {@link RID}.
 * 
 * @author Eric Thill
 *
 */
public class IOOutputs extends IOListener {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Transport transport;
	private final IOValueSerializer valueSerializer = new IOValueSerializer();
	private final IOValuesSerializer valuesSerializer = new IOValuesSerializer();
	
	IOOutputs(Transport transport) {
		super(transport, IOGroups.OUTPUT, CapType.OUTPUT);
		this.transport = transport;
	}
	
	public void setOutput(IOValue output) {
		RID serviceID = getServiceID(output.getID());
		if(serviceID == null) {
			logger.error("Cannot send unrecognized output ID: " + output.getID());
		}
		transport.ucast().send(serviceID, output, valueSerializer);
 	}
	
	public void setOutputs(List<IOValue> outputs) {
		if(outputs.size() == 0) {
			return;
		}
		boolean allToSameService = true;
		RID serviceID = getServiceID(outputs.get(0).getID());
		for(IOValue v : outputs) {
			RID sid = getServiceID(v.getID());
			if(!sid.equals(serviceID)) {
				allToSameService = false;
				break;
			}
		}
		if(allToSameService) {
			// send as batch to single service (more efficient)
			transport.ucast().send(serviceID, outputs, valuesSerializer);
		} else {
			// send one-by-one (not all to same service)
			for(IOValue o : outputs) {
				setOutput(o);
			}
		}
 	}
	
}
