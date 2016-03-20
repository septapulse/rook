package rook.api;

/**
 * Manages {@link Service}s and allows them to communicate with one another.
 * 
 * @author Eric Thill
 *
 */
public interface Router {
	/**
	 * Add a service to the rook environment. This must be called before
	 * start().
	 * 
	 * @param id
	 * @param service
	 */
	void addService(RID id, Service service);

	/**
	 * Initialize services and starts routing messages between them. This method
	 * will call {@link Service#setTransport(rook.api.transport.Transport)} and
	 * {@link Service#init())} for all services before routing any messages
	 * between them.
	 * 
	 * @throws InitException
	 *             When the router or any service could not be initialized. When
	 *             this is thrown, all services that may have already been
	 *             started prior to the exception being thrown will have
	 *             {@link Service#shutdown())} called automatically.
	 */
	void start() throws InitException;

	/**
	 * Stops routing messages, stops all threads, and calls
	 * {@link Service#shutdown())} for all services. This method may only be
	 * called after a successful call to {@link #start()}
	 */
	void shutdown();
}
