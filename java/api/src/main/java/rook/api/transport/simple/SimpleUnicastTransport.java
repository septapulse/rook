package rook.api.transport.simple;

import java.util.concurrent.atomic.AtomicBoolean;

import rook.api.RID;
import rook.api.collections.AtomicCollection;
import rook.api.collections.ThreadSafeCollection;
import rook.api.transport.GrowableBuffer;
import rook.api.transport.Deserializer;
import rook.api.transport.Serializer;
import rook.api.transport.UnicastTransport;
import rook.api.transport.consumer.DeserializingFilteringUnicastMessageConsumer;
import rook.api.transport.consumer.FilteringUnicastMessageConsumer;
import rook.api.transport.consumer.PassthroughUnicastMessageConsumer;
import rook.api.transport.consumer.ProxyUnicastMessageConsumer;
import rook.api.transport.consumer.UnicastMessageConsumer;

/**
 * Generic implementation of a {@link UnicastMessage} that provides logic to
 * manage and dispatch messages to consumers. It uses a {@link Publisher}
 * implementation to send events to the process's {@link Router}.
 * 
 * @author Eric Thill
 *
 */
public class SimpleUnicastTransport implements UnicastTransport {

	private final ThreadSafeCollection<ProxyUnicastMessageConsumer<GrowableBuffer>> messageConsumers = new AtomicCollection<>();
	private final ThreadSafeCollection<ProxyUnicastMessageConsumer<GrowableBuffer>> incognitoMessageConsumers = new AtomicCollection<>();
	private final RID serviceId;
	private final Publisher publisher;
	private final StartIncognitoListenConsumer startIncognitoListenConsumer;
	private final AtomicBoolean incognitoListenStarted = new AtomicBoolean(false);
	
	public SimpleUnicastTransport(RID serviceId, Publisher publisher, StartIncognitoListenConsumer startIncognitoListenConsumer) {
		this.serviceId = serviceId;
		this.publisher = publisher;
		this.startIncognitoListenConsumer = startIncognitoListenConsumer;
	}
	
	@Override
	public void addMessageConsumer(UnicastMessageConsumer<GrowableBuffer> consumer) {
		messageConsumers.add( 
				new PassthroughUnicastMessageConsumer(consumer), false);
	}

	@Override
	public <T> void addMessageConsumer(UnicastMessageConsumer<T> consumer, Deserializer<T> deserializer) {
		messageConsumers.add(
				new DeserializingFilteringUnicastMessageConsumer<>(null, consumer, deserializer), false);
	}

	@Override
	public void addMessageConsumer(RID from, UnicastMessageConsumer<GrowableBuffer> consumer) {
			messageConsumers.add(
					new FilteringUnicastMessageConsumer(from, consumer), false);
	}

	@Override
	public <T> void addMessageConsumer(RID from, UnicastMessageConsumer<T> consumer, Deserializer<T> deserializer) {
		messageConsumers.add(
				new DeserializingFilteringUnicastMessageConsumer<>(from, consumer, deserializer), false);
	}

	@Override
	public <T> void removeMessageConsumer(UnicastMessageConsumer<T> consumer) {
		messageConsumers.removeIf(c -> c.getBaseConsumer() == consumer);
	}

	@Override
	public void send(RID to, GrowableBuffer msg) {
		incognito_send(serviceId, to, msg);
	}

	@Override
	public <T> void send(RID to, T msg, Serializer<T> serializer) {
		GrowableBuffer buf = GrowableBuffer.allocate(0); // FIXME reuse
		serializer.serialize(msg, buf);
		send(to, buf);
	}

	@Override
	public void incognito_addMessageConsumer(UnicastMessageConsumer<GrowableBuffer> consumer) {
		checkIncognitoListenStarted();
		incognitoMessageConsumers.add(
				new PassthroughUnicastMessageConsumer(consumer), false);
	}

	@Override
	public <T> void incognito_addMessageConsumer(UnicastMessageConsumer<T> consumer, Deserializer<T> deserializer) {
		checkIncognitoListenStarted();
		incognitoMessageConsumers.add(
				new DeserializingFilteringUnicastMessageConsumer<>(null, consumer, deserializer), false);
	}

	@Override
	public <T> void incognito_removeMessageConsumer(UnicastMessageConsumer<T> consumer) {
		checkIncognitoListenStarted();
		incognitoMessageConsumers.removeIf(c -> c.getBaseConsumer() == consumer);
	}
	
	private void checkIncognitoListenStarted() {
		if(incognitoListenStarted.compareAndSet(false, true) && startIncognitoListenConsumer != null) {
			startIncognitoListenConsumer.onStartIncognitoListen();
		}
	}

	@Override
	public void incognito_send(RID from, RID to, GrowableBuffer msg) {
		publisher.publish(MessageType.UCAST_MESSAGE, from, to, null, msg);
	}

	@Override
	public <T> void incognito_send(RID from, RID to, T msg, Serializer<T> serializer) {
		GrowableBuffer buf = GrowableBuffer.allocate(0); // FIXME reuse
		serializer.serialize(msg, buf);
		incognito_send(from, to, buf);
	}

	/**
	 * Handle an incoming unicast event by dispatching it to any registered
	 * consumers
	 * 
	 * @param from
	 *            The sending service
	 * @param to
	 *            The service this event was addressed to
	 * @param msg
	 *            The payload
	 */
	public void handleUcastMessage(RID from, RID to, GrowableBuffer msg) {
		if (messageConsumers.size() > 0) {
			if (to.equals(serviceId)) {
				messageConsumers.iterate(c -> {
					c.onUnicastMessage(from, to, msg);
				});
			}
		}
		if (incognitoMessageConsumers.size() > 0) {
			incognitoMessageConsumers.iterate(c -> {
				c.onUnicastMessage(from, to, msg);
			});
		}
	}
}
