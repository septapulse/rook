package rook.core.io.proxy;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import rook.api.RID;
import rook.api.transport.Transport;
import rook.api.transport.event.BroadcastMessage;
import rook.core.io.proxy.message.Cap;
import rook.core.io.proxy.message.CapType;
import rook.core.io.proxy.message.CapsDeserializer;
import rook.core.io.proxy.message.IOValue;
import rook.core.io.proxy.message.PooledValuesDeserializer;

/**
 * Listens for {@link BroadcastMessage}s on the given {@link RID} group. These
 * messages are parsed as {@link IOValue}s and dispatched to registered listeners
 * accordingly.
 * 
 * @author Eric Thill
 *
 */
public abstract class IOListener {

	private final Set<Consumer<IOValue>> consumers = new LinkedHashSet<>();
	private final Set<Consumer<List<IOValue>>> batchConsumers = new LinkedHashSet<>();
	private final Map<RID, Set<Consumer<IOValue>>> filteringConsumers = new HashMap<>();
	private final Map<RID, IOValue> values = new HashMap<>();
	private final Map<RID, RID> valueServiceIDs = new HashMap<>();
	private final Transport transport;
	private final RID group;
	private final CapType type;
	
	IOListener(Transport transport, RID group, CapType type) {
		this.transport = transport;
		this.group = group;
		this.type = type;
		transport.bcast().addMessageConsumer(group, this::handleValues, new PooledValuesDeserializer());
		transport.bcast().addMessageConsumer(IOGroups.CAPS, this::handleCaps, new CapsDeserializer());
	}
	
	public void stop() {
		transport.bcast().removeMessageConsumer(group, this::handleValues);
		transport.bcast().removeMessageConsumer(IOGroups.CAPS, this::handleCaps);
	}
	
	public void reset() {
		values.clear();
		valueServiceIDs.clear();
	}
	
	private void handleCaps(BroadcastMessage<List<Cap>> t) {
		synchronized (valueServiceIDs) {
			for(Cap c : t.getPayload()) {
				if(c.getCapType() == type) {
					valueServiceIDs.put(c.getId().unmodifiable(), t.getFrom().unmodifiable());
				}
			}
		}
	}
	
	private void handleValues(final BroadcastMessage<List<IOValue>> t) {
		for(IOValue v : t.getPayload()) {
			updateCache(v);
			synchronized (consumers) {
				for(Consumer<IOValue> c : consumers) {
					c.accept(v);
				}
			}
			synchronized (filteringConsumers) {
				Set<Consumer<IOValue>> consumers = filteringConsumers.get(v.getID());
				if(consumers != null) {
					for(Consumer<IOValue> c : consumers) {
						c.accept(v);
					}
				}
			}
		}
		synchronized (batchConsumers) {
			for(Consumer<List<IOValue>> c : batchConsumers) {
				c.accept(t.getPayload());
			}
		}
	}

	private void updateCache(IOValue src) {
		synchronized (values) {
			IOValue dest = this.values.get(src.getID());
			if(dest == null) {
				dest = new IOValue();
				this.values.put(src.getID(), dest);
			}
			dest.getID().setValue(src.getID().toValue());
			dest.getValue().copyFrom(src.getValue());
		}
	}

	public void addFilteringConsumer(RID id, Consumer<IOValue> consumer) {
		synchronized (filteringConsumers) {
			Set<Consumer<IOValue>> set = filteringConsumers.get(id);
			if (set == null) {
				set = new LinkedHashSet<>();
				filteringConsumers.put(id.copy(), set);
			}
			set.add(consumer);
		}
	}

	public void removeFilteringConsumer(RID id, Consumer<IOValue> consumer) {
		synchronized (filteringConsumers) {
			Set<Consumer<IOValue>> set = filteringConsumers.get(id);
			if (set != null) {
				set.remove(consumer);
			}
		}
	}
	
	public void addConsumer(Consumer<IOValue> consumer) {
		synchronized (consumers) {
			consumers.add(consumer);
		}
	}

	public void removeConsumer(Consumer<IOValue> consumer) {
		synchronized (consumers) {
			consumers.remove(consumer);
		}
	}
	
	public void addBatchConsumer(Consumer<List<IOValue>> consumer) {
		synchronized (consumers) {
			batchConsumers.add(consumer);
		}
	}

	public void removeBatchConsumer(Consumer<List<IOValue>> consumer) {
		synchronized (consumers) {
			batchConsumers.remove(consumer);
		}
	}

	public IOValue getValue(RID id) {
		synchronized (values) {
			IOValue v = values.get(id);
			return v != null ? v.copy() : null;
		}
	}
	
	RID getServiceID(RID valueID) {
		synchronized (valueServiceIDs) {
			return valueServiceIDs.get(valueID);
		}
	}
	
}
