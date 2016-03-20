package rook.core.io.service;

import java.io.IOException;

import rook.api.InitException;
import rook.core.io.proxy.message.Cap;
import rook.core.io.proxy.message.IOValue;

/**
 * A single input that can be used by an {@link IOService}
 * 
 * @author Eric Thill
 *
 */
public interface IOInput {
	void init() throws InitException;
	void shutdown();
	IOValue read() throws IOException;
	Cap cap();
}
