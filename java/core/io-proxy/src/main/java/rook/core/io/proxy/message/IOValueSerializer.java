package rook.core.io.proxy.message;

import rook.api.transport.GrowableBuffer;
import rook.api.transport.Serializer;

/**
 * Serializes an {@link IOValue} into a buffer
 * 
 * @author Eric Thill
 *
 */
public class IOValueSerializer implements Serializer<IOValue> {
	
	private final GrowableBuffer payload = GrowableBuffer.allocate(128);
	
	@Override
	public GrowableBuffer serialize(IOValue v) {
		int length = v.getSerializedLength();
		payload.reserve(length, false);
		
		payload.setLength(0);
		v.serialize(payload);
		return payload;
	}

}
