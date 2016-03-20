package rook.core.io.service;

import java.io.IOException;

import rook.api.InitException;
import rook.core.io.proxy.message.Cap;
import rook.core.io.proxy.message.IOValue;

/**
 * A single output that can be used by an {@link IOService}
 * 
 * @author Eric Thill
 *
 */
public interface IOOutput {
	void init() throws InitException;
	void shutdown();
	void write(IOValue value) throws IOException;
	Cap cap();
}
