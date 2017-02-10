package run.rook.core.io.proxy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import run.rook.api.RID;
import run.rook.api.Service;
import run.rook.api.transport.GrowableBuffer;
import run.rook.api.transport.Transport;
import run.rook.api.transport.consumer.BroadcastMessageConsumer;
import run.rook.api.util.Sleep;
import run.rook.core.io.proxy.message.Cap;
import run.rook.core.io.proxy.message.CapsDeserializer;

/**
 * Takes care of all wire-level communications to IO {@link Service}s in 
 * the environment.
 * 
 * @author Eric Thill
 *
 */
public class IOProxy {

	private static final GrowableBuffer PROBE_PAYLOAD = GrowableBuffer.allocate(0);
		
	public static Map<RID, List<Cap>> probe(Transport transport, long timeout) {
		Map<RID, List<Cap>> results = new LinkedHashMap<>();
		BroadcastMessageConsumer<List<Cap>> capsListener = new BroadcastMessageConsumer<List<Cap>>() {
			@Override
			public void onBroadcastMessage(RID from, RID group, List<Cap> caps) {
				results.put(from.immutable(), caps);
			}
		};
		transport.bcast().addMessageConsumer(IOGroups.CAPS, null, capsListener, new CapsDeserializer());
		boolean newJoin = transport.bcast().join(IOGroups.CAPS);
		transport.bcast().send(IOGroups.PROBE, PROBE_PAYLOAD);
		Sleep.trySleep(timeout);
		if(newJoin) {
			transport.bcast().leave(IOGroups.CAPS);
		}
		transport.bcast().removeMessageConsumer(capsListener);
		return results;
	}
	
	private final IOOutputs outputs;
	private final IOInputs inputs;
	
	public IOProxy(Transport transport, RID ioServiceId) {
		outputs = new IOOutputs(transport, ioServiceId);
		inputs = new IOInputs(transport, ioServiceId);
	}
	
	/**
	 * Stop listening and shutdown all threads
	 */
	public void stop() {
		inputs.stop();
		outputs.stop();
	}
	
	/**
	 * Reset the internal state
	 */
	public void reset() {
		inputs.reset();
		outputs.reset();
	}
	
	/**
	 * Get the object that can listen to inputs from any IO Service
	 * 
	 * @return IOInputs
	 */
	public IOInputs inputs() {
		return inputs;
	}
	
	/**
	 * Get the object that can listen to and set the outputs from any IO Service
	 * 
	 * @return IOOutputs
	 */
	public IOOutputs outputs() {
		return outputs;
	}

}
