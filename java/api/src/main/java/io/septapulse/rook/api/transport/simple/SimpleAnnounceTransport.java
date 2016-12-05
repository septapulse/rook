package io.septapulse.rook.api.transport.simple;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.collections.AtomicCollection;
import io.septapulse.rook.api.collections.ThreadSafeCollection;
import io.septapulse.rook.api.transport.AnnounceTransport;
import io.septapulse.rook.api.transport.consumer.AnnouncementConsumer;
import io.septapulse.rook.api.transport.consumer.ProbeConsumer;

/**
 * Generic implementation of an {@link AnnounceTransport} that provides logic to
 * manage and dispatch messages to consumers. It uses a {@link Publisher}
 * implementation to send events to the process's {@link Router}.
 * 
 * @author Eric Thill
 *
 */
public class SimpleAnnounceTransport implements AnnounceTransport {

	private final ThreadSafeCollection<AnnouncementConsumer> announceConsumers = new AtomicCollection<>();
	private final ThreadSafeCollection<ProbeConsumer> probeConsumers = new AtomicCollection<>();

	private final RID serviceId;
	private final Publisher publisher;
	private final boolean respondToProbes;

	public SimpleAnnounceTransport(RID serviceId, Publisher publisher, boolean respondToProbes) {
		this.serviceId = serviceId;
		this.publisher = publisher;
		this.respondToProbes = respondToProbes;
	}
	
	@Override
	public void addAnnouncementConsumer(final AnnouncementConsumer consumer) {
		announceConsumers.add(consumer, false);
	}
	
	@Override
	public void removeAnnouncementConsumer(final AnnouncementConsumer consumer) {
		announceConsumers.removeAll(consumer);
	}
	
	@Override
	public void addProbeConsumer(ProbeConsumer consumer) {
		probeConsumers.add(consumer, false);
	}
	
	@Override
	public void removeProbeConsumer(ProbeConsumer consumer) {
		probeConsumers.removeAll(consumer);
	}

	@Override
	public void probe() {
		publisher.publish(MessageType.PROBE, serviceId, null, null, null);
	}

	@Override
	public void incognito_announce(final RID service) {
		publisher.publish(MessageType.ANNOUNCE, service, null, null, null);
	}

	/**
	 * Handle an incoming announcement event by dispatching it to any registered
	 * consumers
	 * 
	 * @param from
	 *            The announced service
	 */
	public void handleAnnouncement(final RID from) {
		announceConsumers.iterate(c -> c.onAnnouncement(from));
	}
	
	/**
	 * Handle an incoming probe event by responding with an ANNOUNCE event.
	 * 
	 * @param requestingService
	 *            The service that sent the probe
	 */
	public void handleProbe(RID from) {
		if(respondToProbes) {
			publisher.publish(MessageType.ANNOUNCE, serviceId, null, null, null);
		}
		probeConsumers.iterate(c -> c.onProbe(from));
	}

}
