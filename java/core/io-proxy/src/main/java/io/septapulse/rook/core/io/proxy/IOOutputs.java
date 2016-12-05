package io.septapulse.rook.core.io.proxy;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.Service;
import io.septapulse.rook.api.transport.GrowableBuffer;
import io.septapulse.rook.api.transport.Transport;
import io.septapulse.rook.core.io.proxy.message.CapType;
import io.septapulse.rook.core.io.proxy.message.IOValue;

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
	
	IOOutputs(Transport transport) {
		super(transport, IOGroups.OUTPUT, CapType.OUTPUT);
		this.transport = transport;
	}
	
	public void setOutput(RID id, IOValue value) {
		RID serviceID = getServiceID(id);
		if(serviceID == null) {
			logger.error("Cannot send unrecognized output ID: " + id);
			return;
		}
		// FIXME reuse buffer
		GrowableBuffer buf = GrowableBuffer.allocate(8+value.getSerializedLength());
		buf.direct().putLong(0, id.toValue());
		buf.length(8);
		value.serialize(buf);
		transport.ucast().send(serviceID, buf);
 	}
	
	public void setOutputs(Map<RID, IOValue> outputs) {
		if(outputs.size() == 0) {
			return;
		}
		boolean allToSameService = true;
		RID serviceID = getServiceID(outputs.entrySet().iterator().next().getKey());
		for(Map.Entry<RID, IOValue> e : outputs.entrySet()) {
			RID sid = getServiceID(e.getKey());
			if(!sid.equals(serviceID)) {
				allToSameService = false;
				break;
			}
		}
		if(allToSameService) {
			// send as batch to single service (more efficient)
			int serializedSize = 0;
			for(Map.Entry<RID, IOValue> e : outputs.entrySet()) {
				serializedSize+=8+e.getValue().getSerializedLength();
			}
			// FIXME reuse buffer
			GrowableBuffer buf = GrowableBuffer.allocate(serializedSize);
			buf.length(0);
			for(Map.Entry<RID, IOValue> e : outputs.entrySet()) {
				buf.direct().putLong(buf.length(), e.getKey().toValue());
				buf.length(buf.length()+8);
				e.getValue().serialize(buf);
			}
			transport.ucast().send(serviceID, buf);
		} else {
			// send one-by-one (not all to same service)
			for(Map.Entry<RID, IOValue> e : outputs.entrySet()) {
				setOutput(e.getKey(), e.getValue());
			}
		}
 	}
	
}
