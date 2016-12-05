package io.septapulse.rook.core.io.service;

import java.io.IOException;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.exception.InitException;
import io.septapulse.rook.core.io.proxy.message.Cap;
import io.septapulse.rook.core.io.proxy.message.IOValue;

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
	RID id();
	Cap cap();
}
