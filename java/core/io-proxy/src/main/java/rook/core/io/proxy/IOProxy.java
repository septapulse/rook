package rook.core.io.proxy;

import rook.api.Service;
import rook.api.transport.Transport;
import rook.core.io.proxy.message.Cap;

/**
 * Takes care of all wire-level communications to IO {@link Service}s in 
 * the environment.
 * 
 * @author Eric Thill
 *
 */
public class IOProxy {

	private final IOOutputs outputs;
	private final IOInputs inputs;
	private final CapsCache capsCache;
	
	public IOProxy(Transport transport) {
		outputs = new IOOutputs(transport);
		inputs = new IOInputs(transport);
		capsCache = new CapsCache(transport);
		capsCache.requestCaps();
	}
	
	/**
	 * Stop listening and shutdown all threads
	 */
	public void stop() {
		inputs.stop();
		outputs.stop();
		capsCache.stop();
	}
	
	/**
	 * Reset the internal state
	 */
	public void reset() {
		inputs.reset();
		outputs.reset();
		capsCache.reset();
		capsCache.requestCaps();
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
	
	/**
	 * Get the cache for all IO Service {@link Cap}s
	 * 
	 * @return CapsCache
	 */
	public CapsCache caps() {
		return capsCache;
	}

}
