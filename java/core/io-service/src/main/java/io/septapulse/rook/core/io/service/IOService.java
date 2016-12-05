package io.septapulse.rook.core.io.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.Service;
import io.septapulse.rook.api.exception.InitException;
import io.septapulse.rook.api.transport.GrowableBuffer;
import io.septapulse.rook.api.transport.Transport;
import io.septapulse.rook.core.io.proxy.IOGroups;
import io.septapulse.rook.core.io.proxy.message.IOValue;

public abstract class IOService implements Service {

	protected static final long DEFAULT_BROADCAST_INTERVAL = 500;
	protected static final long DEFAULT_RECONNECT_INTERVAL = 5000;

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final InputBroadcastLoop inputBcastLoop = new InputBroadcastLoop();
	private final long bcastInterval;
	private final long reconnectInterval;
	private final CapsSerializer capsSerializer = new CapsSerializer();
	private final GrowableBuffer reusedCapsMessage = GrowableBuffer.allocate(128);
	private final GrowableBuffer reusedInputMessage = GrowableBuffer.allocate(128);
	private final RID reusedWriteOutputId = new RID();
	private final IOValue reusedWriteOutputVal = new IOValue(0);
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
		transport.ucast().addMessageConsumer(this::handleOutput);
		transport.bcast().addMessageConsumer(IOGroups.PROBE, null, this::handleProbe);
		transport.bcast().join(IOGroups.PROBE);
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
	
	private void handleProbe(RID from, RID group, GrowableBuffer msg) {
		sendCapsMessage();
	}
	
	private void sendCapsMessage() {
		transport.bcast().send(IOGroups.CAPS, getCapsMessage());
	}
	
	protected GrowableBuffer getCapsMessage() {
		capsSerializer.serialize(ioManager.getCaps(), reusedCapsMessage);
		return reusedCapsMessage;
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
	
	public void handleOutput(RID from, RID to, GrowableBuffer msg) {
		try {
			writeOutputs(msg);
		} catch (IOException e) {
			logger.error("Failed to write to device: " + msg, e);
		}
	}
	
	protected void writeOutputs(GrowableBuffer message) throws IOException {
		// alert the world of the change
		transport.bcast().send(IOGroups.OUTPUT, message);
		onWriteStart();
		// step through each output
		int off = 0;
		while(off < message.length()) {
			// deserialize
			reusedWriteOutputId.setValue(message.direct().getLong(off));
			off+=8;
			off+=reusedWriteOutputVal.deserialize(message, off);
			// handle single value
			writeOutput(reusedWriteOutputId, reusedWriteOutputVal);
		}
		onWriteEnd();
	}
	
	protected void writeOutput(RID id, IOValue value) throws IOException {
		IOOutput output = ioManager.getOutput(id);
		if(output != null) {
			output.write(value);
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
						long start = System.currentTimeMillis();
						GrowableBuffer msg = readInputMessage();
						transport.bcast().send(IOGroups.INPUT, msg);
						long bcastTime = System.currentTimeMillis()-start;
						long sleep = bcastInterval-bcastTime;
						if(sleep > 0)
							Thread.sleep(sleep);
					} catch (Throwable t) {
						logger.error("Failed to read from device. Waiting " + reconnectInterval
								+ " milliseconds before trying again.", t);
						Thread.sleep(reconnectInterval);
					}
				}
			} catch (InterruptedException e) {

			}
		}
		
	}
	
	protected GrowableBuffer readInputMessage() throws IOException {
		onReadStart();
		readInputs(reusedInputMessage);
		onReadEnd();
		return reusedInputMessage;
	}
	
	protected void onReadStart() {
		// Can be used by implementing class to trigger a batch-read
	}
	
	protected void onReadEnd() {
		// Can be used by implementing class to trigger a batch-read
	}
	
	public void readInputs(GrowableBuffer dest) {
		ioManager.forEachInput(this::readInput);
	}
	
	private void readInput(IOInput input) {
		try {
			RID id = input.id();
			IOValue val = input.read();
			reusedInputMessage.reserve(reusedInputMessage.length()+8+val.getSerializedLength(), true);
			
			// serialize ID
			reusedInputMessage.direct().putLong(reusedInputMessage.length(), id.toValue());
			reusedInputMessage.length(reusedInputMessage.length()+8);
			
			// serialize value
			val.serialize(reusedInputMessage);
			
			if(logger.isTraceEnabled()) {
				logger.trace("Input " + id + " = " + val);
			}
		} catch (IOException e) {
			logger.error("Could not read IOValue", e);
		}
	}
	
}
