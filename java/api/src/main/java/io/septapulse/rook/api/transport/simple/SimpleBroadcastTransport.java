package io.septapulse.rook.api.transport.simple;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.collections.AtomicCollection;
import io.septapulse.rook.api.collections.ThreadSafeCollection;
import io.septapulse.rook.api.transport.BroadcastTransport;
import io.septapulse.rook.api.transport.Deserializer;
import io.septapulse.rook.api.transport.GrowableBuffer;
import io.septapulse.rook.api.transport.Serializer;
import io.septapulse.rook.api.transport.consumer.BroadcastJoinConsumer;
import io.septapulse.rook.api.transport.consumer.BroadcastLeaveConsumer;
import io.septapulse.rook.api.transport.consumer.BroadcastMessageConsumer;
import io.septapulse.rook.api.transport.consumer.DeserializingFilteringBroadcastMessageConsumer;
import io.septapulse.rook.api.transport.consumer.FilteringBroadcastMessageConsumer;
import io.septapulse.rook.api.transport.consumer.PassthroughBroadcastMessageConsumer;
import io.septapulse.rook.api.transport.consumer.ProxyBroadcastMessageConsumer;

/**
 * Generic implementation of a {@link BroadcastTransport} that provides logic to
 * manage and dispatch messages to consumers. It uses a {@link Publisher}
 * implementation to send events to the process's {@link Router}.
 * 
 * @author Eric Thill
 *
 */
public class SimpleBroadcastTransport implements BroadcastTransport {

	private final ThreadSafeCollection<ProxyBroadcastMessageConsumer<GrowableBuffer>> messageConsumers = new AtomicCollection<>();
	private final ThreadSafeCollection<BroadcastJoinConsumer> joinConsumers = new AtomicCollection<>();
	private final ThreadSafeCollection<BroadcastLeaveConsumer> leaveConsumers = new AtomicCollection<>();
	private final ThreadSafeCollection<ProxyBroadcastMessageConsumer<GrowableBuffer>> incognitoMessageConsumers = new AtomicCollection<>();
	private final ThreadSafeCollection<RID> joinedGroups = new AtomicCollection<>();
	
	private final RID serviceId;
	private final Publisher publisher;

	public SimpleBroadcastTransport(RID serviceId, Publisher publisher) {
		this.serviceId = serviceId;
		this.publisher = publisher;
	}
	
	public void handleProbe() {
		// resend group joins
		joinedGroups.iterate(group -> incognito_join(serviceId, group));
	}

	@Override
	public void addMessageConsumer(BroadcastMessageConsumer<GrowableBuffer> consumer) {
		messageConsumers.add(
				new PassthroughBroadcastMessageConsumer(consumer), false);
	}
	
	@Override
	public void addMessageConsumer(RID group, RID from, BroadcastMessageConsumer<GrowableBuffer> consumer) {
		messageConsumers.add(
				new FilteringBroadcastMessageConsumer(group, from, consumer), false);
	}

	@Override
	public <T> void addMessageConsumer(RID group, RID from, BroadcastMessageConsumer<T> consumer,
			Deserializer<T> deserializer) {
		messageConsumers.add(
				new DeserializingFilteringBroadcastMessageConsumer<>(group, from, consumer, deserializer), false);
	}
	
	public <T> void removeMessageConsumer(BroadcastMessageConsumer<T> consumer) {
		messageConsumers.removeIf(existing -> existing.getBaseConsumer() == consumer);
	}
	
	@Override
	public void join(RID group) {
		joinedGroups.add(group, false);
		incognito_join(serviceId, group);
	}

	@Override
	public void leave(RID group) {
		joinedGroups.removeAll(group);
		incognito_leave(serviceId, group);
	}
	
	@Override
	public void addJoinConsumer(BroadcastJoinConsumer consumer) {
		joinConsumers.add(consumer, false);
	}

	@Override
	public void removeJoinConsumer(BroadcastJoinConsumer consumer) {
		joinConsumers.removeIf(existing -> consumer == existing);
	}

	@Override
	public void addLeaveConsumer(BroadcastLeaveConsumer consumer) {
		leaveConsumers.add(consumer, false);
	}

	@Override
	public void removeLeaveConsumer(BroadcastLeaveConsumer consumer) {
		leaveConsumers.removeAll(consumer);
	}

	@Override
	public void send(RID group, GrowableBuffer msg) {
		incognito_send(serviceId, group, msg);
	}

	@Override
	public <T> void send(RID group, T message, Serializer<T> serializer) {
		GrowableBuffer buf = GrowableBuffer.allocate(0); // FIXME reuse
		serializer.serialize(message, buf);
		send(group, buf);
	}

	@Override
	public void incognito_send(RID from, RID group, GrowableBuffer msg) {
		publisher.publish(MessageType.BCAST_MESSAGE, from, null, group, msg);
	}

	@Override
	public <T> void incognito_send(RID fromService, RID group, T message, Serializer<T> serializer) {
		GrowableBuffer buf = GrowableBuffer.allocate(0); // FIXME reuse
		serializer.serialize(message, buf);
		incognito_send(fromService, group, buf);
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
	public void incognito_addMessageConsumer(BroadcastMessageConsumer<GrowableBuffer> consumer) {
		incognitoMessageConsumers.add(new PassthroughBroadcastMessageConsumer(consumer), false);
	}
	
	@Override
	public void incognito_removeMessageConsumer(BroadcastMessageConsumer<GrowableBuffer> consumer) {
		incognitoMessageConsumers.removeIf(existing -> existing.getBaseConsumer().equals(consumer));
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
		joinConsumers.iterate(c -> c.onBroadcastJoin(from, group));
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
		leaveConsumers.iterate(c -> c.onBroadcastLeave(from, group));
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
		if(joinedGroups.contains(group)) {
			messageConsumers.iterate(c -> c.onBroadcastMessage(from, group, msg));
		}
		incognitoMessageConsumers.iterate(c -> c.onBroadcastMessage(from, group, msg));
	}

}
