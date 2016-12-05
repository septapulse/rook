package io.septapulse.rook.core.io.service.dummy;

import java.util.List;

import io.septapulse.rook.core.io.proxy.message.DataType;
import io.septapulse.rook.core.io.service.IOServiceConfig;

/**
 * Configuration for a {@link DummyIOService}
 * 
 * @author Eric Thill
 *
 */
public class DummyIOServiceConfig extends IOServiceConfig {
	
	private List<Entry> inputs;
	private List<Entry> outputs;
	
	public List<Entry> getInputs() {
		return inputs;
	}
	
	public List<Entry> getOutputs() {
		return outputs;
	}
	
	public static class Entry {
		private String id;
		private DataType type;
		
		public String getId() {
			return id;
		}
		
		public DataType getType() {
			return type;
		}
	}
}
