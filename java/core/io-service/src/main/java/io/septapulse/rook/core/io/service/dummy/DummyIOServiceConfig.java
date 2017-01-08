package io.septapulse.rook.core.io.service.dummy;

import java.util.List;

import io.septapulse.rook.api.config.Configurable;
import io.septapulse.rook.core.io.proxy.message.DataType;

/**
 * Configuration for a {@link DummyIOService}
 * 
 * @author Eric Thill
 *
 */
public class DummyIOServiceConfig {
	
	@Configurable(min="1", increment="1", 
			comment="Number of milliseconds between broadcasting IOInput changes",
			defaultValue="100")
	private long broadcastInterval = 100;
	private List<Entry> inputs;
	private List<Entry> outputs;
	
	public List<Entry> getInputs() {
		return inputs;
	}

	public DummyIOServiceConfig setBroadcastInterval(long broadcastInterval) {
		this.broadcastInterval = broadcastInterval;
		return this;
	}
	
	public final long getBroadcastInterval() {
		return broadcastInterval;
	}
	
	public DummyIOServiceConfig setInputs(List<Entry> inputs) {
		this.inputs = inputs;
		return this;
	}
	
	public List<Entry> getOutputs() {
		return outputs;
	}
	
	public DummyIOServiceConfig setOutputs(List<Entry> outputs) {
		this.outputs = outputs;
		return this;
	}
	
	public static class Entry {
		private String id;
		private DataType type;
		
		public String getId() {
			return id;
		}
		
		public Entry setId(String id) {
			this.id = id;
			return this;
		}
		
		public DataType getType() {
			return type;
		}
		
		public Entry setType(DataType type) {
			this.type = type;
			return this;
		}
	}
}
