package io.septapulse.rook.core.io.proxy;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.Service;
import io.septapulse.rook.api.transport.GrowableBuffer;
import io.septapulse.rook.api.transport.Transport;
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

	private final Transport transport;
	private final RID ioServiceId;
	private final AtomicLong nextUniqueId = new AtomicLong();
	
	IOOutputs(Transport transport, RID ioServiceId) {
		super(transport, ioServiceId, IOGroups.OUTPUT);
		this.transport = transport;
		this.ioServiceId = ioServiceId;
	}
	
	public void setOutput(RID id, IOValue value) {
		// FIXME reuse buffer
		GrowableBuffer buf = GrowableBuffer.allocate(8+value.getSerializedLength());
		buf.length(17);
		buf.direct().putLong(0, nextUniqueId.getAndIncrement());
		buf.direct().putByte(8, IOMessageConst.SET_OUTPUT);
		buf.direct().putLong(9, id.toValue());
		value.serialize(buf);
		transport.ucast().send(ioServiceId, buf);
 	}
	
	public void setOutputs(Map<RID, IOValue> outputs) {
		if(outputs.size() == 0) {
			return;
		}
		int serializedSize = 0;
		for(Map.Entry<RID, IOValue> e : outputs.entrySet()) {
			serializedSize+=8+e.getValue().getSerializedLength();
		}
		// FIXME reuse buffer
		GrowableBuffer buf = GrowableBuffer.allocate(serializedSize);
		buf.length(9);
		buf.direct().putLong(0, nextUniqueId.getAndIncrement());
		buf.direct().putByte(8, IOMessageConst.SET_OUTPUT);
		for(Map.Entry<RID, IOValue> e : outputs.entrySet()) {
			buf.direct().putLong(buf.length(), e.getKey().toValue());
			buf.length(buf.length()+8);
			e.getValue().serialize(buf);
		}
		transport.ucast().send(ioServiceId, buf);
 	}
	
}
