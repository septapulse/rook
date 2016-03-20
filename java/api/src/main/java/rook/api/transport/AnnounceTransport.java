package rook.api.transport;

import java.util.function.Consumer;

import rook.api.RID;

/**
 * Transport for service announcement and probes
 * 
 * @author Eric Thill
 *
 */
public interface AnnounceTransport {

	/**
	 * Add a consumer for announcement messages
	 * 
	 * @param consumer
	 */
	void addAnnouncementConsumer(Consumer<RID> consumer);

	/**
	 * Send a probe for all service's that are currently online.
	 */
	void probe();

	/**
	 * Send a service announcement on behalf of another service. This method is
	 * intended to be used by services that are able to connect multiple routers
	 * into a single environment.
	 * 
	 * @param service
	 *            The ID of the service being announced
	 */
	void incognito_announce(RID service);

}
