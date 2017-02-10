package run.rook.core.io.proxy;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import run.rook.api.RID;
import run.rook.api.transport.GrowableBuffer;
import run.rook.api.transport.Transport;
import run.rook.api.transport.consumer.BroadcastMessageConsumer;
import run.rook.core.io.proxy.message.IOValue;

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
	private final Set<IOUpdateListener> updateListeners = new LinkedHashSet<>();
	private final Map<RID, Set<IOValueConsumer>> filteringConsumers = new HashMap<>();
	private final Map<RID, IOValue> values = new HashMap<>();
	private final Transport transport;
	private final RID ioServiceId;
	private final RID group;
	private AtomicBoolean joined = new AtomicBoolean(false);
	
	IOListener(Transport transport, RID ioServiceId, RID group) {
		this.transport = transport;
		this.ioServiceId = ioServiceId;
		this.group = group;
		transport.bcast().join(group);
	}
	
	public void stop() {
		if(joined.compareAndSet(true, false)) {
			tryLeave();
		}
	}
	
	public void reset() {
		values.clear();
	}
	
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
			}
			synchronized (updateListeners) {
				for(IOUpdateListener c : updateListeners) {
					c.onUpdate();
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
	
	/**
	 * 
	 * Consumer to receive new values as soon as they arrive
	 * 
	 * @param consumer
	 */
	public void addConsumer(IOValueConsumer consumer) {
		tryJoin();
		synchronized (consumers) {
			consumers.add(consumer);
		}
	}

	public void removeConsumer(IOValueConsumer consumer) {
		synchronized (consumers) {
			consumers.remove(consumer);
			if(consumers.size() == 0) {
				tryLeave();
			}
		}
	}
	
	/**
	 * 
	 * Listeners are called immediately after new input updates arrive
	 * 
	 * @param consumer
	 */
	public void addUpdateListener(IOUpdateListener listener) {
		synchronized (updateListeners) {
			updateListeners.add(listener);
		}
	}

	public void removeUpdateListener(IOUpdateListener listener) {
		synchronized (updateListeners) {
			updateListeners.remove(listener);
		}
	}
	
	private void tryJoin() {
		if(joined.compareAndSet(false, true)) {
			transport.bcast().addMessageConsumer(group, ioServiceId, valueListener);
		}
	}
	
	private void tryLeave() {
		if(joined.compareAndSet(true, false)) {
			transport.bcast().removeMessageConsumer(valueListener);
		}
	}

	/**
	 * Get's the value from the internal value cache.  This cache is updated from IO broadcast messages.
	 * 
	 * @param id The ID to get the value of
	 * @return The value, or null if it does not exist.
	 */
	public IOValue getValue(RID id) {
		synchronized (values) {
			IOValue v = values.get(id);
			return v != null ? v.copy() : null;
		}
	}
	
}
