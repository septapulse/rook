package io.septapulse.rook.core.io.service;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.Service;
import io.septapulse.rook.api.exception.InitException;
import io.septapulse.rook.api.transport.GrowableBuffer;
import io.septapulse.rook.api.transport.Transport;
import io.septapulse.rook.core.io.proxy.IOGroups;
import io.septapulse.rook.core.io.proxy.IOMessageConst;
import io.septapulse.rook.core.io.proxy.message.IOValue;

public abstract class AbstractIOService implements Service {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Map<RID, IOInput> inputs = new LinkedHashMap<>();
	private final Map<RID, IOOutput> outputs = new LinkedHashMap<>();
	private final GrowableBuffer broadcastBuffer = GrowableBuffer.allocate(128);
	private final GrowableBuffer responseBuffer = GrowableBuffer.allocate(128);
	private final RID requestId = RID.create(0);
	private final IOValue requestValue = new IOValue();
	private final Map<RID, IOValue> lastOutputValues = Collections.synchronizedMap(new LinkedHashMap<>());
	private final long broadcastInterval;
	protected Transport transport;
	private volatile boolean run;
	private volatile boolean initializing;
	
	public AbstractIOService(long broadcastInterval) {
		this.broadcastInterval = broadcastInterval;
	}
	
	protected abstract void onInit() throws InitException;
	protected abstract void onShutdown();
	
	protected void addInput(IOInput input) {
		if(!initializing) {
			// thread safety check
			throw new IllegalStateException("addInput(IOInput) must be called during onInit()");
		}
		inputs.put(input.id(), input);
	}

	protected void addOutput(IOOutput output) {
		if(!initializing) {
			// thread safety check
			throw new IllegalStateException("addOutput(IOOutput) must be called during onInit()");
		}
		outputs.put(output.id(), output);
	}

	
	@Override
	public final void setTransport(Transport transport) {
		this.transport = transport;
	}

	@Override
	public void init() throws InitException {
		initializing = true;
		onInit();
		initializing = false;
		transport.ucast().addMessageConsumer(this::handleUnicast);
		transport.bcast().addMessageConsumer(this::handleBroadcast);
		transport.bcast().join(IOGroups.PROBE);
		run = true;
		new Thread(this::broadcastLoop, getClass().getSimpleName() + " Broadcaster").start();
	}
	
	public void handleUnicast(RID from, RID to, GrowableBuffer payload) {
		if(payload.length() >= 9) {
			long uid = payload.direct().getLong(0);
			byte type = payload.direct().getByte(1);
			switch(type) {
			case IOMessageConst.GET_CAPABILITIES:
				responseBuffer.length(10);
				responseBuffer.direct().putLong(0, uid);
				responseBuffer.direct().putByte(8, type);
				responseBuffer.direct().putByte(9, IOMessageConst.SUCCESS);
				serializeCapabilities(responseBuffer);
				transport.ucast().send(from, responseBuffer);
				break;
			case IOMessageConst.GET_INPUT:
				requestId.setValue(payload.direct().getLong(1));
				IOInput input = inputs.get(requestId);
				IOValue inputVal;
				try {
					inputVal = input != null ? input.read() : null;
				} catch (IOException e) {
					logger.error("Could not read from input '" + requestId + "'", e);
					inputVal = null;
				}
				responseBuffer.length(10);
				responseBuffer.direct().putLong(0, uid);
				responseBuffer.direct().putByte(8, type);
				if(inputVal == null) {
					// failed
					responseBuffer.direct().putByte(9, IOMessageConst.FAILED);
				} else {
					responseBuffer.direct().putByte(9, IOMessageConst.SUCCESS);
					inputVal.serialize(responseBuffer);
				}
				transport.ucast().send(from, responseBuffer);
				break;
			case IOMessageConst.GET_OUTPUT:
				requestId.setValue(payload.direct().getLong(1));
				IOValue outputVal = lastOutputValues.get(requestId);
				responseBuffer.length(10);
				responseBuffer.direct().putLong(0, uid);
				responseBuffer.direct().putByte(8, type);
				if(outputVal == null) {
					// failed
					responseBuffer.direct().putByte(9, IOMessageConst.FAILED);
				} else {
					responseBuffer.direct().putByte(9, IOMessageConst.SUCCESS);
					outputVal.serialize(responseBuffer);
				}
				transport.ucast().send(from, responseBuffer);
				break;
			case IOMessageConst.SET_OUTPUT:
				requestId.setValue(payload.direct().getLong(1));
				requestValue.deserialize(payload, 2);
				responseBuffer.length(10);
				responseBuffer.direct().putLong(0, uid);
				responseBuffer.direct().putByte(8, type);
				IOOutput output = outputs.get(requestId);
				if(output == null) {
					logger.error("Received SET_OUTPUT command for '" + requestId + "' which does not exist");
					responseBuffer.direct().putByte(9, IOMessageConst.FAILED);
				} else {
					try {
						output.write(requestValue);
						responseBuffer.direct().putByte(9, IOMessageConst.SUCCESS);
						transport.ucast().send(from, responseBuffer);
					} catch(IOException e) {
						logger.error("Could not SET_OUTPUT for '" + requestId + "'", e);
						responseBuffer.direct().putByte(9, IOMessageConst.FAILED);
						transport.ucast().send(from, responseBuffer);
					}
				}
				break;
			default:
				logger.warn("Received unrecognized unicast message type: '" + payload.bytes()[0] + "' from '" + from + "'");
				break;
			}
		} else {
			logger.warn("Received empty unicast message from '" + from + "'");
		}
	}
	
	public void handleBroadcast(RID from, RID group, GrowableBuffer payload) {
		if(IOGroups.PROBE.equals(group)) {
			GrowableBuffer buffer = GrowableBuffer.allocate(128);
			serializeCapabilities(buffer);
			transport.bcast().send(IOGroups.CAPS, buffer);
		}
	}
	
	private void broadcastLoop() {
		while(run) {
			try {
				// send inputs broadcast message
				broadcastBuffer.length(0);
				serializeInputValues(broadcastBuffer);
				transport.bcast().send(IOGroups.INPUT, broadcastBuffer);
				
				// send outputs broadcast message
				broadcastBuffer.length(0);
				serializeOutputValues(broadcastBuffer);
				transport.bcast().send(IOGroups.OUTPUT, broadcastBuffer);
				
				// sleep
				Thread.sleep(broadcastInterval);
			} catch (InterruptedException e) {
				logger.error("Interrupted", e);
			}
		}
	}
	
	private void serializeInputValues(GrowableBuffer buffer) {
		for(IOInput input : inputs.values()) {
			try {
				int off = buffer.length();
				buffer.reserve(off+8, true);
				buffer.direct().putLong(off, input.id().toValue());
				buffer.length(off+8);
				input.read().serialize(buffer);
			} catch (IOException e) {
				logger.error("Could not read from " + input.id().toString(), e);
			}
		}
	}

	private void serializeOutputValues(GrowableBuffer buffer) {
		synchronized (lastOutputValues) {
			for(Map.Entry<RID, IOValue> e : lastOutputValues.entrySet()) {
				int off = buffer.length();
				buffer.reserve(off+8, true);
				buffer.direct().putLong(off, e.getKey().toValue());
				buffer.length(off+8);
				e.getValue().serialize(buffer);
			}
		}
	}
	
	private void serializeCapabilities(GrowableBuffer buffer) {
		for(IOInput input : inputs.values()) {
			input.cap().serialize(buffer);
		}
		for(IOOutput output : outputs.values()) {
			output.cap().serialize(buffer);
		}
	}

	@Override
	public final void shutdown() {
		onShutdown();
		run = false;
	}
	
}
