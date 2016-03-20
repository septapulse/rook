package rook.api.transport.consumer;

import java.util.function.Consumer;

import rook.api.RID;
import rook.api.transport.GrowableBuffer;
import rook.api.transport.event.BroadcastMessage;

/**
 * Filters the incoming {@link BroadcastMessage} by group and by the sending
 * service ID. The purpose of this class is to be able to easily separate
 * message filtering and application logic.
 * 
 * @author Eric Thill
 *
 */
public class FilteringBroadcastMessageConsumer implements Consumer<BroadcastMessage<GrowableBuffer>> {

	private final RID filterGroup;
	private final RID filterFrom;
	private final Consumer<BroadcastMessage<GrowableBuffer>> consumer;

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
			Consumer<BroadcastMessage<GrowableBuffer>> consumer) {
		this.filterGroup = filterGroup;
		this.filterFrom = filterFrom;
		this.consumer = consumer;
	}

	@Override
	public void accept(BroadcastMessage<GrowableBuffer> t) {
		boolean accept = true;
		if (filterGroup != null && !t.getGroup().equals(filterGroup)) {
			accept = false;
		}
		if (filterFrom != null && !t.getFrom().equals(filterFrom)) {
			accept = false;
		}
		if (accept) {
			consumer.accept(t);
		}
	}
}
