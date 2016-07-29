package rook.api.transport;

import rook.api.RID;
import rook.api.transport.consumer.AnnouncementConsumer;
import rook.api.transport.consumer.ProbeConsumer;

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
	void addAnnouncementConsumer(AnnouncementConsumer consumer);

	/**
	 * Remove a consumer for announcement messages
	 * 
	 * @param consumer
	 */
	void removeAnnouncementConsumer(AnnouncementConsumer consumer);

	/**
	 * Add a consumer for probes
	 * 
	 * @param consumer
	 */
	void addProbeConsumer(ProbeConsumer consumer);
	
	/**
	 * Remove a consumer for probes
	 * 
	 * @param consumer
	 */
	void removeProbeConsumer(ProbeConsumer consumer);
	
	/**
	 * Send a probe for all service's that are currently online. Services will
	 * respond with an announcement followed by any Broadcast Groups they are
	 * currently joined to.
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
