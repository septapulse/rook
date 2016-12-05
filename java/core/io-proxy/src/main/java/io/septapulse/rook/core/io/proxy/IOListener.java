package io.septapulse.rook.core.io.proxy;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.transport.GrowableBuffer;
import io.septapulse.rook.api.transport.Transport;
import io.septapulse.rook.api.transport.consumer.BroadcastMessageConsumer;
import io.septapulse.rook.core.io.proxy.message.Cap;
import io.septapulse.rook.core.io.proxy.message.CapType;
import io.septapulse.rook.core.io.proxy.message.CapsDeserializer;
import io.septapulse.rook.core.io.proxy.message.IOValue;

/**
 * Listens for {@link BroadcastMessage}s on the given {@link RID} group. These
 * messages are parsed as {@link IOValue}s and dispatched to registered listeners
 * accordingly.
 * 
 * @author Eric Thill
 *
 */
public abstract class IOListener {

	private final Set<IOValueConsumer> consumers = new LinkedHashSet<>();
	private final Set<IOValueBatchConsumer> batchConsumers = new LinkedHashSet<>();
	private final Map<RID, Set<IOValueConsumer>> filteringConsumers = new HashMap<>();
	private final Map<RID, IOValue> values = new HashMap<>();
	private final Map<RID, RID> valueServiceIDs = new HashMap<>();
	private final Transport transport;
	private final RID group;
	private final CapType type;
	private AtomicBoolean joined = new AtomicBoolean(false);
	
	IOListener(Transport transport, RID group, CapType type) {
		this.transport = transport;
		this.group = group;
		this.type = type;
		transport.bcast().addMessageConsumer(IOGroups.CAPS, null, capsListener, new CapsDeserializer());
		transport.bcast().join(IOGroups.CAPS);
	}
	
	public void stop() {
		if(joined.get()) {
			transport.bcast().removeMessageConsumer(valueListener);
			transport.bcast().leave(group);
		}
		transport.bcast().removeMessageConsumer(capsListener);
		transport.bcast().leave(IOGroups.CAPS);
	}
	
	public void reset() {
		values.clear();
		valueServiceIDs.clear();
	}
	
	private final BroadcastMessageConsumer<List<Cap>> capsListener = new BroadcastMessageConsumer<List<Cap>>() {
		@Override
		public void onBroadcastMessage(RID from, RID group, List<Cap> caps) {
			synchronized (valueServiceIDs) {
				for(Cap c : caps) {
					if(c.getCapType() == type) {
						valueServiceIDs.put(c.getId().immutable(), from.immutable());
					}
				}
			}
		}
	};
	
	private final BroadcastMessageConsumer<GrowableBuffer> valueListener = new BroadcastMessageConsumer<GrowableBuffer>() {
		private RID id = new RID();
		private IOValue val = new IOValue();
		@Override
		public void onBroadcastMessage(RID from, RID group, GrowableBuffer valuesBuf) {
			int off = 0;
			while(off < valuesBuf.length()) {
				// deserialize
				id.setValue(valuesBuf.direct().getLong(off));
				off+=8;
				off+=val.deserialize(valuesBuf, off);
				
				// update cache
				updateCache(id, val);
				
				// dispatch
				// FIXME iterator garbage creation
				synchronized (consumers) {
					for(IOValueConsumer c : consumers) {
						c.onValue(id, val);
					}
				}
				synchronized (filteringConsumers) {
					Set<IOValueConsumer> consumers = filteringConsumers.get(id);
					if(consumers != null) {
						for(IOValueConsumer c : consumers) {
							c.onValue(id, val);
						}
					}
				}
				synchronized (batchConsumers) {
					for(IOValueBatchConsumer c : batchConsumers) {
						c.onValue(id, val, off == valuesBuf.length());
					}
				}
			}
		}
	};

	private void updateCache(RID id, IOValue val) {
		synchronized (values) {
			IOValue dest = this.values.get(id);
			if(dest == null) {
				dest = new IOValue();
				this.values.put(id.immutable(), dest);
			}
			dest.copyFrom(val);
		}
	}

	public void addFilteringConsumer(RID id, IOValueConsumer consumer) {
		tryJoin();
		synchronized (filteringConsumers) {
			Set<IOValueConsumer> set = filteringConsumers.get(id);
			if (set == null) {
				set = new LinkedHashSet<>();
				filteringConsumers.put(id.copy(), set);
			}
			set.add(consumer);
		}
	}

	public void removeFilteringConsumer(RID id, IOValueConsumer consumer) {
		synchronized (filteringConsumers) {
			Set<IOValueConsumer> set = filteringConsumers.get(id);
			if (set != null) {
				set.remove(consumer);
			}
		}
	}
	
	public void addConsumer(IOValueConsumer consumer) {
		tryJoin();
		synchronized (consumers) {
			consumers.add(consumer);
		}
	}

	public void removeConsumer(IOValueConsumer consumer) {
		synchronized (consumers) {
			consumers.remove(consumer);
		}
	}
	
	public void addBatchConsumer(IOValueBatchConsumer consumer) {
		tryJoin();
		synchronized (consumers) {
			batchConsumers.add(consumer);
		}
	}

	public void removeBatchConsumer(Consumer<List<IOValue>> consumer) {
		synchronized (consumers) {
			batchConsumers.remove(consumer);
		}
	}
	
	private void tryJoin() {
		if(joined.compareAndSet(false, true)) {
			transport.bcast().addMessageConsumer(group, null, valueListener);
			transport.bcast().join(group);
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
