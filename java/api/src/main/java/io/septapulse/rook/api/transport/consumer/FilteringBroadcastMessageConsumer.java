package io.septapulse.rook.api.transport.consumer;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.transport.GrowableBuffer;

/**
 * Filters the incoming {@link BroadcastMessage} by group and by the sending
 * service ID. The purpose of this class is to be able to easily separate
 * message filtering and application logic.
 * 
 * @author Eric Thill
 *
 */
public class FilteringBroadcastMessageConsumer implements ProxyBroadcastMessageConsumer<GrowableBuffer> {

	private final RID filterGroup;
	private final RID filterFrom;
	private final BroadcastMessageConsumer<GrowableBuffer> consumer;

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
	public FilteringBroadcastMessageConsumer(RID filterGroup, RID filterFrom,
			BroadcastMessageConsumer<GrowableBuffer> consumer) {
		this.filterGroup = filterGroup;
		this.filterFrom = filterFrom;
		this.consumer = consumer;
	}
	
	@Override
	public void onBroadcastMessage(RID from, RID group, GrowableBuffer payload) {
		if (filterGroup != null && !group.equals(filterGroup)) {
			return;
		}
		if (filterFrom != null && !from.equals(filterFrom)) {
			return;
		}
		consumer.onBroadcastMessage(from, group, payload);
	}
	
	@Override
	public BroadcastMessageConsumer<GrowableBuffer> getBaseConsumer() {
		return consumer;
	}
}
