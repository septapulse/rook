package rook.api.transport.simple;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import rook.api.RID;
import rook.api.Router;
import rook.api.transport.BroadcastTransport;
import rook.api.transport.Deserializer;
import rook.api.transport.GrowableBuffer;
import rook.api.transport.Serializer;
import rook.api.transport.consumer.CopyingBroadcastMessageConsumer;
import rook.api.transport.consumer.DeserializingBroadcastMessageConsumer;
import rook.api.transport.consumer.FilteringBroadcastMessageConsumer;
import rook.api.transport.event.BroadcastJoin;
import rook.api.transport.event.BroadcastLeave;
import rook.api.transport.event.BroadcastMessage;

/**
 * Generic implementation of a {@link BroadcastTransport} that provides logic to
 * manage and dispatch messages to consumers. It uses a {@link Publisher}
 * implementation to send events to the process's {@link Router}.
 * 
 * @author Eric Thill
 *
 */
public class SimpleBroadcastTransport implements BroadcastTransport {

	private final Map<Consumer<?>, Consumer<BroadcastMessage<GrowableBuffer>>> messageConsumers = Collections
			.synchronizedMap(new LinkedHashMap<>());
	private final Set<Consumer<BroadcastJoin>> joinConsumers = Collections.synchronizedSet(new LinkedHashSet<>());
	private final Set<Consumer<BroadcastLeave>> leaveConsumer = Collections.synchronizedSet(new LinkedHashSet<>());
	private final Map<RID, Integer> broadcastGroupCounts = new HashMap<>();
	private final BroadcastMessage<GrowableBuffer> bcastMsg = new BroadcastMessage<>();
	private final BroadcastJoin bcastJoin = new BroadcastJoin();
	private final BroadcastLeave bcastLeave = new BroadcastLeave();
	private final Set<Consumer<BroadcastMessage<GrowableBuffer>>> incognitoMessageConsumers = Collections.synchronizedSet(new LinkedHashSet<>());

	private final RID serviceId;
	private final Publisher publisher;
	private final int defaultMessageCapacity;

	public SimpleBroadcastTransport(RID serviceId, Publisher publisher, int defaultMessageCapacity) {
		this.serviceId = serviceId;
		this.publisher = publisher;
		this.defaultMessageCapacity = defaultMessageCapacity;
	}

	@Override
	public void addMessageConsumer(RID group, Consumer<BroadcastMessage<GrowableBuffer>> consumer) {
		addMessageConsumerHelper(group, null, consumer,
				new CopyingBroadcastMessageConsumer(defaultMessageCapacity, consumer));
	}

	@Override
	public <T> void addMessageConsumer(RID group, Consumer<BroadcastMessage<T>> consumer,
			Deserializer<T> deserializer) {
		addMessageConsumerHelper(group, null, consumer,
				new DeserializingBroadcastMessageConsumer<>(consumer, deserializer));
	}

	@Override
	public void addMessageConsumer(RID group, RID from, Consumer<BroadcastMessage<GrowableBuffer>> consumer) {
		addMessageConsumerHelper(group, from, consumer,
				new CopyingBroadcastMessageConsumer(defaultMessageCapacity, consumer));
	}

	@Override
	public <T> void addMessageConsumer(RID group, RID from, Consumer<BroadcastMessage<T>> consumer,
			Deserializer<T> deserializer) {
		addMessageConsumerHelper(group, from, consumer,
				new DeserializingBroadcastMessageConsumer<>(consumer, deserializer));
	}

	private <T> void addMessageConsumerHelper(RID group, RID from, Consumer<BroadcastMessage<T>> userConsumer,
			Consumer<BroadcastMessage<GrowableBuffer>> bufferConsumer) {
		Consumer<BroadcastMessage<GrowableBuffer>> filteringConsumer = group != null || from != null
				? new FilteringBroadcastMessageConsumer(group, from, bufferConsumer) : bufferConsumer;

		synchronized (messageConsumers) {
			messageConsumers.put(userConsumer, filteringConsumer);
			if (broadcastGroupCounts.containsKey(group)) {
				broadcastGroupCounts.put(group, broadcastGroupCounts.get(group) + 1);
			} else {
				incognito_join(serviceId, group);
				broadcastGroupCounts.put(group, 1);
			}
		}
	}

	@Override
	public <T> void removeMessageConsumer(RID group, Consumer<BroadcastMessage<T>> consumer) {
		synchronized (messageConsumers) {
			if (messageConsumers.remove(consumer) != null) {
				int newGroupCount = broadcastGroupCounts.get(group) - 1;
				if (newGroupCount == 0) {
					broadcastGroupCounts.remove(group);
					incognito_leave(serviceId, group);
				} else {
					broadcastGroupCounts.put(group, newGroupCount);
				}
			}
		}
	}

	@Override
	public void addJoinConsumer(Consumer<BroadcastJoin> consumer) {
		synchronized (joinConsumers) {
			joinConsumers.add(consumer);
		}
	}

	@Override
	public void removeJoinConsumer(Consumer<BroadcastJoin> consumer) {
		synchronized (joinConsumers) {
			joinConsumers.remove(consumer);
		}
	}

	@Override
	public void addLeaveConsumer(Consumer<BroadcastLeave> consumer) {
		synchronized (leaveConsumer) {
			leaveConsumer.add(consumer);
		}
	}

	@Override
	public void removeLeaveConsumer(Consumer<BroadcastLeave> consumer) {
		synchronized (leaveConsumer) {
			leaveConsumer.remove(consumer);
		}
	}

	@Override
	public void send(RID group, GrowableBuffer msg) {
		incognito_send(serviceId, group, msg);
	}

	@Override
	public <T> void send(RID group, T message, Serializer<T> serializer) {
		send(group, serializer.serialize(message));
	}

	@Override
	public void incognito_send(RID from, RID group, GrowableBuffer msg) {
		publisher.publish(MessageType.BCAST_MESSAGE, from, null, group, msg);
	}

	@Override
	public <T> void incognito_send(RID fromService, RID group, T message, Serializer<T> serializer) {
		incognito_send(fromService, group, serializer.serialize(message));
	}

	@Override
	public void incognito_join(RID from, RID group) {
		publisher.publish(MessageType.BCAST_JOIN, from, null, group, null);
	}

	@Override
	public void incognito_leave(RID from, RID group) {
		publisher.publish(MessageType.BCAST_LEAVE, from, null, group, null);
	}
	
	@Override
	public void incognito_addMessageConsumer(Consumer<BroadcastMessage<GrowableBuffer>> consumer) {
		incognitoMessageConsumers.add(consumer);
	}
	
	@Override
	public void incognito_removeMessageConsumer(Consumer<BroadcastMessage<GrowableBuffer>> consumer) {
		incognitoMessageConsumers.remove(consumer);
	}

	/**
	 * Handle an incoming join event by dispatching it to any registered
	 * consumers
	 * 
	 * @param from
	 *            The joining service
	 * @param group
	 *            The group being joined
	 */
	public void handleBcastJoin(RID from, RID group) {
		synchronized (joinConsumers) {
			if (joinConsumers.size() > 0) {
				bcastJoin.setFrom(from);
				bcastJoin.setGroup(group);
				for (Consumer<BroadcastJoin> l : joinConsumers) {
					l.accept(bcastJoin);
				}
			}
		}
	}

	/**
	 * Handle an incoming leave event by dispatching it to any registered
	 * consumers
	 * 
	 * @param from
	 *            The leaving service
	 * @param group
	 *            The group being left
	 */
	public void handleBcastLeave(RID from, RID group) {
		synchronized (leaveConsumer) {
			if (leaveConsumer.size() > 0) {
				bcastLeave.setFrom(from);
				bcastLeave.setGroup(group);
				for (Consumer<BroadcastLeave> l : leaveConsumer) {
					l.accept(bcastLeave);
				}
			}
		}
	}

	/**
	 * Handle an incoming broadcast event by dispatching it to any registered
	 * consumers
	 * 
	 * @param from
	 *            The sending service
	 * @param group
	 *            The sending group
	 * @param msg
	 *            The payload
	 */
	public void handleBcastMessage(RID from, RID group, GrowableBuffer msg) {
		synchronized (messageConsumers) {
			if (messageConsumers.size() > 0 || incognitoMessageConsumers.size() > 0) {
				bcastMsg.getFrom().setValue(from.toValue());
				bcastMsg.getGroup().setValue(group.toValue());
				bcastMsg.setPayload(msg);
				for (Consumer<BroadcastMessage<GrowableBuffer>> l : messageConsumers.values()) {
					l.accept(bcastMsg);
				}
				for (Consumer<BroadcastMessage<GrowableBuffer>> l : incognitoMessageConsumers) {
					l.accept(bcastMsg);
				}
			}
		}
	}

}
