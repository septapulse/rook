package rook.api;

import rook.api.exception.InitException;
import rook.api.transport.Transport;

/**
 * A process that can be started to routes messages between services. Service
 * use {@link Transport} implementations to communicate with each other. Often
 * times a {@link Router} is responsible for delivery between
 * {@link Transport}s.
 * 
 * @author Eric Thill
 *
 */
public interface Router {
	/**
	 * Start the Router
	 * @throws InitException
	 */
	void start() throws InitException;

	/**
	 * Stop the Router
	 */
	void stop();
}
