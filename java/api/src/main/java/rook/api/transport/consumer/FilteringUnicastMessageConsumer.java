package rook.api.transport.consumer;

import java.util.function.Consumer;

import rook.api.RID;
import rook.api.transport.GrowableBuffer;
import rook.api.transport.event.UnicastMessage;

/**
 * Filters the incoming {@link UnicastMessage} by group and by the sending
 * service ID. The purpose of this class is to be able to easily separate
 * message filtering and application logic.
 * 
 * @author Eric Thill
 *
 */
public class FilteringUnicastMessageConsumer implements Consumer<UnicastMessage<GrowableBuffer>> {

	private final RID filterFrom;
	private final Consumer<UnicastMessage<GrowableBuffer>> consumer;

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
	public FilteringUnicastMessageConsumer(RID filterFrom, Consumer<UnicastMessage<GrowableBuffer>> consumer) {
		this.filterFrom = filterFrom;
		this.consumer = consumer;
	}

	@Override
	public void accept(UnicastMessage<GrowableBuffer> t) {
		if (filterFrom == null || t.getFrom().equals(filterFrom)) {
			consumer.accept(t);
		}
	}
}
