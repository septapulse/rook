package io.septapulse.rook.core.io.service;

import java.io.IOException;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.exception.InitException;
import io.septapulse.rook.core.io.proxy.message.Cap;
import io.septapulse.rook.core.io.proxy.message.IOValue;

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
	RID id();
	Cap cap();
}
