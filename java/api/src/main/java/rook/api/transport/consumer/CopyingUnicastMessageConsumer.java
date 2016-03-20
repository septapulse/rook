package rook.api.transport.consumer;

import java.util.function.Consumer;

import rook.api.transport.GrowableBuffer;
import rook.api.transport.event.UnicastMessage;

/**
 * Copies the inbound message to a buffer owned by this consumer for the purpose
 * of preventing modification of a shared message with multiple consumers.
 * 
 * @author Eric Thill
 *
 */
public class CopyingUnicastMessageConsumer implements Consumer<UnicastMessage<GrowableBuffer>> {

	private final Consumer<UnicastMessage<GrowableBuffer>> consumer;
	private final UnicastMessage<GrowableBuffer> copy = new UnicastMessage<>();
	private final GrowableBuffer payload;
	
	/**
	 * CopyingUnicastMessageConsumer constructor
	 * 
	 * @param defaultMessageCapacity
	 *            The default capacity of the intermediate copy buffer
	 * @param consumer
	 *            The consumer that will receive the copied messages
	 */
	public CopyingUnicastMessageConsumer(int defaultMessageCapacity, Consumer<UnicastMessage<GrowableBuffer>> consumer) {
		this.consumer = consumer;
		this.payload = GrowableBuffer.allocate(defaultMessageCapacity);
	}
	
	@Override
	public void accept(UnicastMessage<GrowableBuffer> t) {
		payload.copyFrom(t.getPayload());
		copy.getFrom().setValue(t.getFrom().toValue());
		copy.getTo().setValue(t.getTo().toValue());
		copy.setPayload(payload);
		consumer.accept(copy);
		payload.reset(false);
	}
}
