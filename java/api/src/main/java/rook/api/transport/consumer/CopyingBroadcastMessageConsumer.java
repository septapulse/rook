package rook.api.transport.consumer;

import java.util.function.Consumer;

import rook.api.transport.GrowableBuffer;
import rook.api.transport.event.BroadcastMessage;

/**
 * Copies the inbound message to a buffer owned by this consumer for the purpose
 * of preventing modification of a shared message with multiple consumers.
 * 
 * @author Eric Thill
 *
 */
public class CopyingBroadcastMessageConsumer implements Consumer<BroadcastMessage<GrowableBuffer>> {

	private final Consumer<BroadcastMessage<GrowableBuffer>> consumer;
	private final BroadcastMessage<GrowableBuffer> copy = new BroadcastMessage<>();
	private final GrowableBuffer payload;

	/**
	 * CopyingBroadcastMessageConsumer constructor
	 * 
	 * @param defaultMessageCapacity
	 *            The default capacity of the intermediate copy buffer
	 * @param consumer
	 *            The consumer that will receive the copied messages
	 */
	public CopyingBroadcastMessageConsumer(int defaultMessageCapacity,
			Consumer<BroadcastMessage<GrowableBuffer>> consumer) {
		this.consumer = consumer;
		this.payload = GrowableBuffer.allocate(defaultMessageCapacity);
	}

	@Override
	public void accept(BroadcastMessage<GrowableBuffer> t) {
		payload.copyFrom(t.getPayload());
		copy.getFrom().setValue(t.getFrom().toValue());
		copy.getGroup().setValue(t.getGroup().toValue());
		copy.setPayload(payload);
		consumer.accept(t);
		payload.reset(false);
	}
}
