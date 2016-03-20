package rook.core.io.service;

import java.util.List;

import rook.api.transport.GrowableBuffer;
import rook.api.transport.Serializer;
import rook.core.io.proxy.message.Cap;

/**
 * Serializes a list of {@link Cap}s into a buffer
 * 
 * @author Eric Thill
 *
 */
public class CapsSerializer implements Serializer<List<Cap>> {
	
	private final GrowableBuffer payload = GrowableBuffer.allocate(128);
	
	@Override
	public GrowableBuffer serialize(List<Cap> msg) {
		payload.reset(false);
		int length = 0;
		for(Cap c : msg) {
			length += c.getSerializedSize();
		}
		payload.reserve(length, false);
		
		payload.setLength(0);
		for(Cap c : msg) {
			c.serialize(payload);
		}
		return payload;
	}

}
