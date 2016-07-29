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
	
	@Override
	public void serialize(List<Cap> msg, GrowableBuffer dest) {
		int length = 0;
		for(Cap c : msg) {
			length += c.getSerializedSize();
		}
		dest.reserve(length, false);
		
		dest.length(0);
		for(Cap c : msg) {
			c.serialize(dest);
		}
	}

}
