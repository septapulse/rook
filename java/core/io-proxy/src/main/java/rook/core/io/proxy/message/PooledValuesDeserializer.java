package rook.core.io.proxy.message;

import java.util.ArrayList;
import java.util.List;

import rook.api.transport.Deserializer;
import rook.api.transport.GrowableBuffer;

/**
 * Deserializes a list of {@link IOValue}s from a buffer. This class keeps an
 * underlying pool of values that are used every time deserialize is called.
 * 
 * @author Eric Thill
 *
 */
public class PooledValuesDeserializer implements Deserializer<List<IOValue>> {

	private final IOValue[] pool = new IOValue[128];
	private final List<IOValue> values = new ArrayList<>(128);
	
	@Override
	public List<IOValue> deserialize(GrowableBuffer msg) {
		for(int i = 0; i < values.size(); i++) {
			pool[i] = values.get(i);
		}
		values.clear();
		
		int poolIdx = 0;
		int off = 0;
		while(off < msg.getLength()) {
			IOValue input = pool[poolIdx++];
			if(input == null) {
				input = new IOValue();
			}
			off += input.deserialize(msg, off);
			values.add(input);
		}
		return values;
	}

}
