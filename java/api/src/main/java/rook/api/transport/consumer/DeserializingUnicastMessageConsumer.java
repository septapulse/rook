package rook.api.transport.consumer;

import java.util.function.Consumer;

import rook.api.transport.Deserializer;
import rook.api.transport.GrowableBuffer;
import rook.api.transport.event.UnicastMessage;

/**
 * Automatically deserializes the incoming buffer into a generic object and pass
 * it directly to the underling consumer. The purpose of this class is to be
 * able to easily separate deserialization and application logic.
 * 
 * @author Eric Thill
 *
 * @param <T>
 */
public class DeserializingUnicastMessageConsumer<T> implements Consumer<UnicastMessage<GrowableBuffer>> {

	private final Consumer<UnicastMessage<T>> consumer;
	private final Deserializer<T> deserializer;
	private final UnicastMessage<T> msg = new UnicastMessage<>();

	/**
	 * DeserializingUnicastMessageConsumer constructor
	 * 
	 * @param consumer
	 *            The consumer that will receive the deserialized messages
	 * @param deserializer
	 *            The deserializer responsible for turning the buffer into an
	 *            object to be consumed
	 */
	public DeserializingUnicastMessageConsumer(Consumer<UnicastMessage<T>> consumer, Deserializer<T> deserializer) {
		this.consumer = consumer;
		this.deserializer = deserializer;
	}

	@Override
	public void accept(UnicastMessage<GrowableBuffer> t) {
		T deserialized = deserializer.deserialize(t.getPayload());
		if (deserialized != null) {
			msg.getFrom().setValue(t.getFrom().toValue());
			msg.getTo().setValue(t.getTo().toValue());
			msg.setPayload(deserialized);
			consumer.accept(msg);
		}
	}
}
