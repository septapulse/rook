package run.rook.core.io.service;

import java.io.IOException;

import run.rook.api.RID;
import run.rook.api.exception.InitException;
import run.rook.core.io.proxy.message.Cap;
import run.rook.core.io.proxy.message.IOValue;

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
