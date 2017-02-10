package run.rook.api.transport;

import run.rook.api.RID;
import run.rook.api.Service;
import run.rook.api.exception.ExceptionHandler;
import run.rook.api.exception.InitException;

/**
 * {@link Transport} that can be setup/started/stopped. Typically this is used
 * by the bootstrap code, and is passed to the underlying {@link Service} as a
 * {@link Transport}.
 * 
 * @author Eric Thill
 *
 */
public interface ControllableTransport extends Transport {
	/**
	 * Set the RID associated with this transport/service
	 * 
	 * @param serviceId
	 */
	void setServiceId(RID serviceId);

	/**
	 * Set the asynchronous {@link ExceptionHandler}
	 * 
	 * @param h
	 *            The handler
	 */
	void setExceptionHandler(ExceptionHandler h);

	/**
	 * Set if the transport should send announcements in response to probe
	 * message. Default is true.
	 * 
	 * @param respondToProbes
	 */
	void setRespondToProbes(boolean respondToProbes);

	/**
	 * Start the transport
	 * 
	 * @throws InitException
	 */
	void start() throws InitException;

	/**
	 * Stop the transport
	 */
	void shutdown();
}
