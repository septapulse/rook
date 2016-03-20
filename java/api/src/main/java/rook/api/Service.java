package rook.api;

import rook.api.transport.Transport;

/**
 * Provides some unit of functionality by sending and receiving messages to and
 * from other services.
 * 
 * @author Eric Thill
 *
 */
public interface Service {
	/**
	 * Sets the underlying {@link Transport} to be used. Called once before
	 * {@link #init()}
	 * 
	 * @param transport
	 *            The transport
	 */
	void setTransport(Transport transport);

	/**
	 * Initialize objects, setup message consumers, start threads. The service
	 * may now begin sending messages.
	 * 
	 * @throws InitException
	 *             When initialization was unsuccessful.
	 */
	void init() throws InitException;

	/**
	 * Stop all threads. Guaranteed to only be called after a successful call to
	 * {@link #init()}
	 */
	void shutdown();
}
