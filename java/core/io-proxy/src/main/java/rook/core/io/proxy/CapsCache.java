package rook.core.io.proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rook.api.RID;
import rook.api.Service;
import rook.api.transport.GrowableBuffer;
import rook.api.transport.Transport;
import rook.api.transport.event.BroadcastMessage;
import rook.core.io.proxy.message.Cap;
import rook.core.io.proxy.message.CapsDeserializer;

/**
 * Keeps track of the {@link Cap}s of all IO {@link Service}s in the environment
 * 
 * @author Eric Thill
 *
 */
public class CapsCache {
	
	public static final GrowableBuffer PROBE_PAYLOAD = GrowableBuffer.allocate(0);
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Transport transport;
	private final Set<Consumer<List<Cap>>> consumers = Collections.synchronizedSet(new LinkedHashSet<>());
	private List<Cap> caps = Collections.emptyList();
	private Map<RID, List<Cap>> serviceCaps = new HashMap<>();
	
	public CapsCache(Transport transport) {
		this.transport = transport;
		transport.bcast().addMessageConsumer(IOGroups.CAPS, capsConsumer, new CapsDeserializer());
	}
	
	public void reset() {
		// clear state
		serviceCaps.clear();
		// this also dispatches empty caps to reset consumers
		updateCaps(Collections.emptyList());
		// request any new caps out there
		requestCaps();
	}
	
	void stop() {
		transport.bcast().removeMessageConsumer(IOGroups.CAPS, capsConsumer);
	}
	
	public void requestCaps() {
		transport.bcast().send(IOGroups.PROBE, PROBE_PAYLOAD);
	}

	private final Consumer<BroadcastMessage<List<Cap>>> capsConsumer = new Consumer<BroadcastMessage<List<Cap>>>() {
		@Override
		public void accept(BroadcastMessage<List<Cap>> t) {
			logger.info("Received caps from " + t.getFrom() + ": " + t);
			RID serviceID = t.getFrom();
			List<Cap> caps = t.getPayload();
			synchronized (serviceCaps) {
				serviceCaps.put(serviceID.unmodifiable(), caps);
				
				// update "allServices" caps list
				List<Cap> newCaps = new ArrayList<>();
				for(Map.Entry<RID, List<Cap>> e : serviceCaps.entrySet()) {
					for(Cap c : e.getValue()) {
						newCaps.add(c);
					}
				}
				
				updateCaps(newCaps);
			}
		}
	};
	
	private void updateCaps(List<Cap> caps) {
		CapsCache.this.caps = Collections.unmodifiableList(caps);
		synchronized (consumers) {
			for(Consumer<List<Cap>> c : consumers) {
				c.accept(CapsCache.this.caps);
			}
		}
	}
	
	public List<Cap> getCaps() {
		return caps;
	}
	
	public void addConsumer(Consumer<List<Cap>> c) {
		consumers.add(c);
	}
	
	public void removeConsumer(Consumer<List<Cap>> c) {
		consumers.remove(c);
	}
}
