package rook.api.transport.consumer;

import rook.api.RID;
import rook.api.transport.GrowableBuffer;
import rook.api.transport.Deserializer;

/**
 * Filters and deserializes the incoming {@link BroadcastMessage} by group and
 * by the sending service ID. The purpose of this class is to be able to easily
 * separate message filtering and application logic.
 * 
 * @author Eric Thill
 *
 * @param <T>
 *
 */
public class DeserializingFilteringBroadcastMessageConsumer<T> implements ProxyBroadcastMessageConsumer<GrowableBuffer> {

	private final RID filterGroup;
	private final RID filterFrom;
	private final BroadcastMessageConsumer<T> consumer;
	private final Deserializer<T> deserializer;

	/**
	 * FilteringBroadcastMessageConsumer constructor
	 * 
	 * @param filterGroup
	 *            The group that must match for the message to be forwarded to
	 *            the underlying consumer. A null value will allow all "group"
	 *            fields through.
	 * @param filterFrom
	 *            The "from" service ID that must match for the message to be
	 *            forwarded to the underlying consumer. A null value will allow
	 *            all "from" fields through.
	 * @param consumer
	 *            The underlying consumer
	 */
	public DeserializingFilteringBroadcastMessageConsumer(RID filterGroup, RID filterFrom,
			BroadcastMessageConsumer<T> consumer, Deserializer<T> deserializer) {
		this.filterGroup = filterGroup;
		this.filterFrom = filterFrom;
		this.consumer = consumer;
		this.deserializer = deserializer;
	}

	@Override
	public void onBroadcastMessage(RID from, RID group, GrowableBuffer payload) {
		if (filterGroup != null && !group.equals(filterGroup))
			return;
		if (filterFrom != null && !from.equals(filterFrom))
			return;
		T deserialized = deserializer.deserialize(payload);
		if (deserialized != null) {
			consumer.onBroadcastMessage(from, group, deserialized);
		}
	}

	@Override
	public BroadcastMessageConsumer<T> getBaseConsumer() {
		return consumer;
	}
}
