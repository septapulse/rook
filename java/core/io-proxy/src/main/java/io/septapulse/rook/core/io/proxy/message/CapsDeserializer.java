package io.septapulse.rook.core.io.proxy.message;

import java.util.ArrayList;
import java.util.List;

import io.septapulse.rook.api.transport.Deserializer;
import io.septapulse.rook.api.transport.GrowableBuffer;

/**
 * Deserializes a list of {@link Cap}s from a buffer
 * 
 * @author Eric Thill
 *
 */
public class CapsDeserializer implements Deserializer<List<Cap>> {

	@Override
	public List<Cap> deserialize(GrowableBuffer msg) {
		List<Cap> caps = new ArrayList<>();
		int off = 0;
		while(off < msg.length()) {
			Cap cap = new Cap();
			off += cap.deserialize(msg, off);
			caps.add(cap);
		}
		return caps;
	}

}
