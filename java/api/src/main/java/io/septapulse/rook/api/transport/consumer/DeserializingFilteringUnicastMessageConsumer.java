package io.septapulse.rook.api.transport.consumer;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.transport.Deserializer;
import io.septapulse.rook.api.transport.GrowableBuffer;

/**
 * Filters and deserializes the incoming {@link UnicastMessage} by group and by
 * the sending service ID. The purpose of this class is to be able to easily
 * separate message filtering and application logic.
 * 
 * @author Eric Thill
 *
 * @param <T>
 *
 */
public class DeserializingFilteringUnicastMessageConsumer<T> implements ProxyUnicastMessageConsumer<GrowableBuffer> {

	private final RID filterFrom;
	private final UnicastMessageConsumer<T> consumer;
	private final Deserializer<T> deserializer;

	/**
	 * FilteringUnicastMessageConsumer constructor
	 * 
	 * @param filterFrom
	 *            The "from" service ID that must match for the message to be
	 *            forwarded to the underlying consumer. A null value will allow
	 *            all "from" fields through.
	 * @param consumer
	 *            The underlying consumer
	 */
	public DeserializingFilteringUnicastMessageConsumer(RID filterFrom, 
			UnicastMessageConsumer<T> consumer, Deserializer<T> deserializer) {
		this.filterFrom = filterFrom;
		this.consumer = consumer;
		this.deserializer = deserializer;
	}

	@Override
	public void onUnicastMessage(RID from, RID to, GrowableBuffer payload) {
		if (filterFrom != null && !filterFrom.equals(from))
			return;
		T deserialized = deserializer.deserialize(payload);
		if (deserialized != null) {
			consumer.onUnicastMessage(from, to, deserialized);
		}
	}

	@Override
	public UnicastMessageConsumer<T> getBaseConsumer() {
		return consumer;
	}
}
