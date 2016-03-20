package rook.api.transport.simple;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import rook.api.RID;
import rook.api.Router;
import rook.api.transport.Deserializer;
import rook.api.transport.GrowableBuffer;
import rook.api.transport.Serializer;
import rook.api.transport.UnicastTransport;
import rook.api.transport.consumer.CopyingUnicastMessageConsumer;
import rook.api.transport.consumer.DeserializingUnicastMessageConsumer;
import rook.api.transport.consumer.FilteringUnicastMessageConsumer;
import rook.api.transport.event.UnicastMessage;

/**
 * Generic implementation of a {@link UnicastMessage} that provides logic to
 * manage and dispatch messages to consumers. It uses a {@link Publisher}
 * implementation to send events to the process's {@link Router}.
 * 
 * @author Eric Thill
 *
 */
public class SimpleUnicastTransport implements UnicastTransport {

	private final Map<Consumer<?>, Consumer<UnicastMessage<GrowableBuffer>>> messageConsumers = Collections
			.synchronizedMap(new LinkedHashMap<>());
	private final Set<Consumer<UnicastMessage<GrowableBuffer>>> ingognitoMessageConsumers = Collections
			.synchronizedSet(new LinkedHashSet<>());
	private final UnicastMessage<GrowableBuffer> ucastMsg = new UnicastMessage<>();

	private final RID serviceId;
	private final Publisher publisher;
	private final int defaultMessageCapacity;

	public SimpleUnicastTransport(RID serviceId, Publisher publisher, int defaultMessageCapacity) {
		this.serviceId = serviceId;
		this.publisher = publisher;
		this.defaultMessageCapacity = defaultMessageCapacity;
	}

	@Override
	public void addMessageConsumer(Consumer<UnicastMessage<GrowableBuffer>> consumer) {
		synchronized (messageConsumers) {
			messageConsumers.put(consumer, new CopyingUnicastMessageConsumer(defaultMessageCapacity, consumer));
		}
	}

	@Override
	public <T> void addMessageConsumer(Consumer<UnicastMessage<T>> consumer, Deserializer<T> deserializer) {
		synchronized (messageConsumers) {
			messageConsumers.put(consumer, new DeserializingUnicastMessageConsumer<>(consumer, deserializer));
		}
	}

	@Override
	public void addMessageConsumer(RID from, Consumer<UnicastMessage<GrowableBuffer>> consumer) {
		synchronized (messageConsumers) {
			messageConsumers.put(consumer, new FilteringUnicastMessageConsumer(from,
					new CopyingUnicastMessageConsumer(defaultMessageCapacity, consumer)));
		}
	}

	@Override
	public <T> void addMessageConsumer(RID from, Consumer<UnicastMessage<T>> consumer, Deserializer<T> deserializer) {
		synchronized (messageConsumers) {
			messageConsumers.put(consumer, new FilteringUnicastMessageConsumer(from,
					new DeserializingUnicastMessageConsumer<>(consumer, deserializer)));
		}
	}

	@Override
	public <T> void removeMessageConsumer(Consumer<UnicastMessage<T>> consumer) {
		synchronized (messageConsumers) {
			messageConsumers.remove(consumer);
		}
	}

	@Override
	public void send(RID to, GrowableBuffer msg) {
		incognito_send(serviceId, to, msg);
	}

	@Override
	public <T> void send(RID to, T msg, Serializer<T> serializer) {
		send(to, serializer.serialize(msg));
	}

	@Override
	public void incognito_addMessageConsumer(Consumer<UnicastMessage<GrowableBuffer>> consumer) {
		synchronized (ingognitoMessageConsumers) {
			ingognitoMessageConsumers.add(consumer);
		}
	}

	@Override
	public <T> void incognito_addMessageConsumer(Consumer<UnicastMessage<T>> consumer, Deserializer<T> deserializer) {
		synchronized (ingognitoMessageConsumers) {
			ingognitoMessageConsumers.add(new DeserializingUnicastMessageConsumer<>(consumer, deserializer));
		}
	}

	@Override
	public <T> void incognito_removeMessageConsumer(Consumer<UnicastMessage<T>> consumer) {
		synchronized (ingognitoMessageConsumers) {
			ingognitoMessageConsumers.remove(consumer);
		}
	}

	@Override
	public void incognito_send(RID from, RID to, GrowableBuffer msg) {
		publisher.publish(MessageType.UCAST_MESSAGE, from, to, null, msg);
	}

	@Override
	public <T> void incognito_send(RID from, RID to, T msg, Serializer<T> serializer) {
		incognito_send(from, to, serializer.serialize(msg));
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
		synchronized (messageConsumers) {
			if (messageConsumers.size() > 0) {
				if (to.equals(serviceId)) {
					ucastMsg.getFrom().setValue(from.toValue());
					ucastMsg.getTo().setValue(to.toValue());
					ucastMsg.setPayload(msg);
					for (Consumer<UnicastMessage<GrowableBuffer>> l : messageConsumers.values()) {
						l.accept(ucastMsg);
					}
				}
			}
		}
		synchronized (ingognitoMessageConsumers) {
			if (ingognitoMessageConsumers.size() > 0) {
				ucastMsg.getFrom().setValue(from.toValue());
				ucastMsg.getTo().setValue(to.toValue());
				ucastMsg.setPayload(msg);
				for (Consumer<UnicastMessage<GrowableBuffer>> l : ingognitoMessageConsumers) {
					l.accept(ucastMsg);
				}
			}
		}
	}
}
