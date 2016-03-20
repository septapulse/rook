package rook.core.io.proxy.message;

import java.util.List;

import rook.api.transport.GrowableBuffer;
import rook.api.transport.Serializer;

/**
 * Serializes a list of {@link IOValue}s into a buffer
 * 
 * @author Eric Thill
 *
 */
public class IOValuesSerializer implements Serializer<List<IOValue>> {
	
	private final GrowableBuffer payload = GrowableBuffer.allocate(128);
	
	@Override
	public GrowableBuffer serialize(List<IOValue> msg) {
		int length = 0;
		for(IOValue v : msg) {
			length += v.getSerializedLength();
		}
		payload.reserve(length, false);
		
		payload.setLength(0);
		for(IOValue v : msg) {
			v.serialize(payload);
		}
		return payload;
	}

}
