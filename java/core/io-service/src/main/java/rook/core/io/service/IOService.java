package rook.core.io.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rook.api.InitException;
import rook.api.Service;
import rook.api.transport.GrowableBuffer;
import rook.api.transport.Transport;
import rook.api.transport.event.BroadcastMessage;
import rook.api.transport.event.UnicastMessage;
import rook.core.io.proxy.IOGroups;
import rook.core.io.proxy.message.IOValue;
import rook.core.io.proxy.message.IOValuesSerializer;
import rook.core.io.proxy.message.PooledValuesDeserializer;

public abstract class IOService implements Service {

	protected static final long DEFAULT_BROADCAST_INTERVAL = 500;
	protected static final long DEFAULT_RECONNECT_INTERVAL = 5000;

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final InputBroadcastLoop inputBcastLoop = new InputBroadcastLoop();
	private final long bcastInterval;
	private final long reconnectInterval;
	private final List<IOValue> inputValues = new ArrayList<>();
	private int numInputValues = 0;
	private final IOValuesSerializer valuesSerializer = new IOValuesSerializer();
	private final PooledValuesDeserializer valuesDeserializer = new PooledValuesDeserializer();
	private final CapsSerializer capsSerializer = new CapsSerializer();
	protected final IOManager ioManager = new IOManager();
	
	protected Transport transport;

	public IOService(IOServiceConfig config) throws InitException {
		this(config.getBroadcastInterval(), config.getReconnectInterval());
	}
	
	public IOService(long broadcastInterval, long reconnectInterval) throws InitException {
		this.bcastInterval = broadcastInterval > 0 ? broadcastInterval : DEFAULT_BROADCAST_INTERVAL;
		this.reconnectInterval = reconnectInterval > 0 ? reconnectInterval : DEFAULT_RECONNECT_INTERVAL;
	}

	public abstract void onInit() throws InitException;
	
	@Override
	public final void setTransport(Transport transport) {
		this.transport = transport;
	}

	@Override
	public final void init() throws InitException {
		onInit();
		ioManager.forEachInput(this::initializeInput);
		ioManager.forEachOutput(this::initializeOutput);
		transport.ucast().addMessageConsumer(writeOutputConsumer);
		transport.bcast().addMessageConsumer(IOGroups.PROBE, probeConsumer);
		sendCapsMessage();
		new Thread(inputBcastLoop, "IOService Broadcast Loop").start();
	}
	
	private void initializeInput(IOInput input) {
		try {
			input.init();
		} catch (InitException e) {
			logger.error("Could not iniatialize input", e);
		}
	}
	
	private void initializeOutput(IOOutput output) {
		try {
			output.init();
		} catch (InitException e) {
			logger.error("Could not iniatialize output", e);
		}
	}
	
	private final Consumer<BroadcastMessage<GrowableBuffer>> probeConsumer = new Consumer<BroadcastMessage<GrowableBuffer>>() {
		@Override
		public void accept(BroadcastMessage<GrowableBuffer> t) {
			sendCapsMessage();
		}
	};
	
	private void sendCapsMessage() {
		transport.bcast().send(IOGroups.CAPS, getCapsMessage());
	}
	
	protected GrowableBuffer getCapsMessage() {
		return capsSerializer.serialize(ioManager.getCaps());
	}
	
	@Override
	public final void shutdown() {
		inputBcastLoop.shutdown();
		ioManager.executeShutdownTasks();
		ioManager.forEachInput(i -> i.shutdown());
		ioManager.forEachOutput(o -> o.shutdown());
		onShutdown();
	}
	
	public abstract void onShutdown();
	
	private final Consumer<UnicastMessage<GrowableBuffer>> writeOutputConsumer = new Consumer<UnicastMessage<GrowableBuffer>>() {
		@Override
		public void accept(UnicastMessage<GrowableBuffer> msg) {
			try {
				writeOutputs(msg.getPayload());
			} catch (IOException e) {
				logger.error("Failed to write to device: " + msg.getPayload(), e);
			}
		}
	};
	
	protected void writeOutputs(GrowableBuffer message) throws IOException {
		onWriteStart();
		List<IOValue> outputs = valuesDeserializer.deserialize(message);
		writeOutputs(outputs);
		onWriteEnd();
	}
	
	protected void writeOutputs(List<IOValue> values) throws IOException {
		transport.bcast().send(IOGroups.OUTPUT, values, valuesSerializer);
		for(IOValue value : values) {
			IOOutput output = ioManager.getOutput(value.getID());
			if(output != null) {
				output.write(value);
			}
		}
	}
	
	protected void onWriteStart() {
		// Can be used by implementing class to trigger a batch-write
	}
	
	protected void onWriteEnd() {
		// Can be used by implementing class to trigger a batch-write
	}

	private class InputBroadcastLoop implements Runnable {

		private volatile boolean run = true;

		public void shutdown() {
			run = false;
		}

		@Override
		public void run() {
			try {
				while (run) {
					try {
						GrowableBuffer msg = readInputMessage();
						transport.bcast().send(IOGroups.INPUT, msg);
						// FIXME measure time spent measuring
						Thread.sleep(bcastInterval);
					} catch (IOException e) {
						logger.error("Failed to read from device. Waiting " + reconnectInterval
								+ " milliseconds before trying again.", e);
						Thread.sleep(reconnectInterval);
					}
				}
			} catch (InterruptedException e) {

			}
		}
		
	}
	
	protected GrowableBuffer readInputMessage() throws IOException {
		onReadStart();
		GrowableBuffer buf = valuesSerializer.serialize(readInputs());
		onReadEnd();
		return buf;
	}
	
	protected void onReadStart() {
		// Can be used by implementing class to trigger a batch-read
	}
	
	protected void onReadEnd() {
		// Can be used by implementing class to trigger a batch-read
	}
	
	public List<IOValue> readInputs() {
		numInputValues = 0;
		ioManager.forEachInput(this::readInput);
		return inputValues;
	}
	
	private void readInput(IOInput input) {
		if(numInputValues == inputValues.size()) {
			inputValues.add(new IOValue());
		}
		try {
			inputValues.get(numInputValues).copyFrom(input.read());
			if(logger.isTraceEnabled()) {
				logger.trace("Input " + inputValues.get(numInputValues));
			}
			numInputValues++;
		} catch (IOException e) {
			logger.error("Could not read IOValue", e);
		}
	}
	
}
