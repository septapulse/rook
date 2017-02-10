package run.rook.api.transport.consumer;

import run.rook.api.RID;
import run.rook.api.transport.GrowableBuffer;

/**
 * Filters the incoming {@link UnicastMessage} by group and by the sending
 * service ID. The purpose of this class is to be able to easily separate
 * message filtering and application logic.
 * 
 * @author Eric Thill
 *
 */
public class FilteringUnicastMessageConsumer implements ProxyUnicastMessageConsumer<GrowableBuffer> {

	private final RID filterFrom;
	private final UnicastMessageConsumer<GrowableBuffer> consumer;

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
	public FilteringUnicastMessageConsumer(RID filterFrom, UnicastMessageConsumer<GrowableBuffer> consumer) {
		this.filterFrom = filterFrom;
		this.consumer = consumer;
	}
	
	@Override
	public void onUnicastMessage(RID from, RID to, GrowableBuffer payload) {
		if (filterFrom != null && !filterFrom.equals(from))
			return;
		consumer.onUnicastMessage(from, to, payload);
	}
	
	@Override
	public UnicastMessageConsumer<GrowableBuffer> getBaseConsumer() {
		return consumer;
	}
}
