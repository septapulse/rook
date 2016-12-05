package io.septapulse.rook.core.io.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.core.io.proxy.message.Cap;
import io.septapulse.rook.core.io.proxy.message.IOValue;

/**
 * Manages {@link IOInput} and {@link IOOutput} objects for an {@link IOService}
 * 
 * @author Eric Thill
 *
 */
public class IOManager {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final List<Cap> caps = new ArrayList<>();
	private final List<IOInput> inputs = new ArrayList<>();
	private final Map<RID, IOOutput> outputs = new LinkedHashMap<>();
	private final List<Runnable> shutdownTasks = new ArrayList<>();
	
	public List<Cap> getCaps() {
		return new ArrayList<>(caps);
	}
	
	public synchronized void addInput(RID id, IOInput input) {
		inputs.add(input);
		caps.add(input.cap());
	}
	
	public synchronized void addOutput(RID id, IOOutput output, IOValue shutdownValue) {
		outputs.put(id, output);
		caps.add(output.cap());
		if(shutdownValue != null) {
			shutdownTasks.add(new OutputShutdownTask(id, output, shutdownValue.copy()));
		}
	}
	
	public synchronized void forEachInput(Consumer<IOInput> c) {
		for(IOInput input : inputs) {
			c.accept(input);
		}
	}
	
	public synchronized IOOutput getOutput(RID id) {
		return outputs.get(id);
	}
	
	public synchronized void forEachOutput(Consumer<IOOutput> c) {
		for(IOOutput output : outputs.values()) {
			c.accept(output);
		}
	}
	
	public void executeShutdownTasks() {
		for(Runnable r : shutdownTasks) {
			r.run();
		}
	}
	
	private class OutputShutdownTask implements Runnable {
		private final RID id;
		private final IOOutput output;
		private final IOValue value;
		public OutputShutdownTask(RID id, IOOutput output, IOValue value) {
			this.id = id;
			this.output = output;
			this.value = value;
		}
		@Override
		public void run() {
			try {
				output.write(value);
			} catch (IOException e) {
				logger.info("Could not write shutdown value to " + id);
			}
		}
	}
}
