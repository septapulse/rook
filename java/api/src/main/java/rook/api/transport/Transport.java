package rook.api.transport;

import java.security.Provider.Service;

/**
 * Used by a {@link Service} to send and receive messages
 * 
 * @author Eric Thill
 *
 */
public interface Transport {
	/**
	 * Get the underlying transport responsible for announcement messages
	 * 
	 * @return The {@link AnnounceTransport}
	 */
	AnnounceTransport announce();
	
	/**
	 * Get the underlying transport responsible for broadcast messages
	 * 
	 * @return The {@link BroadcastTransport}
	 */
	BroadcastTransport bcast();
	
	/**
	 * Get the underlying transport responsible for unicast message
	 * 
	 * @return The {@link UnicastTransport}
	 */
	UnicastTransport ucast();
}
