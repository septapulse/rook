package rook.api.transport.simple;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import rook.api.RID;
import rook.api.Router;
import rook.api.transport.AnnounceTransport;

/**
 * Generic implementation of an {@link AnnounceTransport} that provides logic to
 * manage and dispatch messages to consumers. It uses a {@link Publisher}
 * implementation to send events to the process's {@link Router}.
 * 
 * @author Eric Thill
 *
 */
public class SimpleAnnounceTransport implements AnnounceTransport {

	private final Set<Consumer<RID>> announceListeners = Collections.synchronizedSet(new LinkedHashSet<>());

	private final RID serviceId;
	private final Publisher publisher;

	public SimpleAnnounceTransport(RID serviceId, Publisher publisher) {
		this.serviceId = serviceId;
		this.publisher = publisher;
	}

	@Override
	public void addAnnouncementConsumer(Consumer<RID> consumer) {
		synchronized (announceListeners) {
			announceListeners.add(consumer);
		}
	}

	@Override
	public void probe() {
		publisher.publish(MessageType.PROBE, serviceId, null, null, null);
	}

	@Override
	public void incognito_announce(RID service) {
		publisher.publish(MessageType.ANNOUNCE, service, null, null, null);
	}

	/**
	 * Handle an incoming announcement event by dispatching it to any registered
	 * consumers
	 * 
	 * @param from
	 *            The announced service
	 */
	public void handleAnnouncement(RID from) {
		synchronized (announceListeners) {
			for (Consumer<RID> l : announceListeners) {
				l.accept(from);
			}
		}
	}

	/**
	 * Handle an incoming probe event by responding with an ANNOUNCE event.
	 * 
	 * @param requestingService
	 *            The service that sent the probe
	 */
	public void handleProbe(RID requestingService) {
		publisher.publish(MessageType.ANNOUNCE, serviceId, requestingService, null, null);
	}

}
