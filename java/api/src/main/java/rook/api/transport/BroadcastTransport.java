package rook.api.transport;

import java.util.function.Consumer;

import rook.api.RID;
import rook.api.transport.event.BroadcastJoin;
import rook.api.transport.event.BroadcastLeave;
import rook.api.transport.event.BroadcastMessage;

/**
 * Transport for broadcast messages. Broadcast messages are sent once and
 * delivered to all services that are currently connected to the environment.
 * 
 * @author Eric Thill
 *
 */
public interface BroadcastTransport {

	/**
	 * Add a broadcast message consumer for the given group. The given group
	 * will be joined if the transport is not already subscribed to it.
	 * 
	 * @param group
	 *            The group to join and listen to
	 * @param consumer
	 *            The message consumer
	 */
	void addMessageConsumer(RID group, Consumer<BroadcastMessage<GrowableBuffer>> consumer);

	/**
	 * Add a deserializing broadcast message consumer for the given group. The
	 * given group will be joined if the transport is not already subscribed to
	 * it.
	 * 
	 * @param group
	 *            The group to join and listen to
	 * @param consumer
	 *            The message consumer
	 * @param deserializer
	 *            The deserializer that converts a binary message into an object
	 *            to be consumed
	 */
	<T> void addMessageConsumer(RID group, Consumer<BroadcastMessage<T>> consumer, Deserializer<T> deserializer);

	/**
	 * Add a broadcast message consumer for the given group from a specified
	 * service. The given group will be joined if the transport is not already
	 * subscribed to it.
	 * 
	 * @param group
	 *            The group to join and listen to
	 * @param from
	 *            The service to listen to. Only messages from this service ID
	 *            on the given group will be passed to the consumer.
	 * @param consumer
	 *            The message consumer
	 */
	void addMessageConsumer(RID group, RID from, Consumer<BroadcastMessage<GrowableBuffer>> consumer);

	/**
	 * Add a deserializing broadcast message consumer for the given group from a
	 * specified service. The given group will be joined if the transport is not
	 * already subscribed to it.
	 * 
	 * @param group
	 *            The group to join and listen to
	 * @param from
	 *            The service to listen to. Only messages from this service ID
	 *            on the given group will be passed to the consumer.
	 * @param consumer
	 *            The message consumer
	 * @param deserializer
	 *            The deserializer that converts a binary message into an object
	 *            to be consumed
	 */
	<T> void addMessageConsumer(RID group, RID from, Consumer<BroadcastMessage<T>> consumer,
			Deserializer<T> deserializer);

	/**
	 * Remove a consumer for the given group. If this was the only consumer for
	 * the given group, the appropriate leave event will be published.
	 * 
	 * @param group
	 *            The group to leave
	 * @param consumer
	 *            The consumer to remove
	 */
	<T> void removeMessageConsumer(RID group, Consumer<BroadcastMessage<T>> consumer);

	/**
	 * Add a consumer to listen to JOIN events
	 * 
	 * @param consumer
	 *            The consumer
	 */
	void addJoinConsumer(Consumer<BroadcastJoin> consumer);

	/**
	 * Remove a JOIN event consumer
	 * 
	 * @param consumer
	 *            The consumer
	 */
	void removeJoinConsumer(Consumer<BroadcastJoin> consumer);

	/**
	 * Add a consumer to listen to LEAVE events
	 * 
	 * @param consumer
	 *            The consumer
	 */
	void addLeaveConsumer(Consumer<BroadcastLeave> consumer);

	/**
	 * Remove a LEAVE event consumer
	 * 
	 * @param consumer
	 *            The consumer
	 */
	void removeLeaveConsumer(Consumer<BroadcastLeave> consumer);

	/**
	 * Send a broadcast message for the given group
	 * 
	 * @param group
	 *            The broadcast group. Any services that have joined this group
	 *            will receive the message.
	 * @param message
	 *            The message
	 */
	void send(RID group, GrowableBuffer message);

	/**
	 * Serialize and send a broadcast message for the given group. This method is
	 * intended to be used by services that are able to connect multiple routers
	 * into a single environment.
	 * 
	 * @param group
	 *            The broadcast group. Any services that have joined this group
	 *            will receive the message.
	 * @param message
	 *            The unserialized message
	 * @param serializer
	 *            The serializer that will serialize the message
	 */
	<T> void send(RID group, T message, Serializer<T> serializer);

	/**
	 * Send a JOIN event on behalf of another service. This method is
	 * intended to be used by services that are able to connect multiple routers
	 * into a single environment.
	 * 
	 * @param fromService
	 *            The "sending" service
	 * @param group
	 *            The group
	 */
	void incognito_join(RID fromService, RID group);

	/**
	 * Send a LEAVE event on behalf of another service. This method is
	 * intended to be used by services that are able to connect multiple routers
	 * into a single environment.
	 * 
	 * @param fromService
	 *            The "sending" service
	 * @param group
	 *            The group
	 */
	void incognito_leave(RID fromService, RID group);

	/**
	 * Send a broadcast message on behalf of another service. This method is
	 * intended to be used by services that are able to connect multiple routers
	 * into a single environment.
	 * 
	 * @param fromService
	 *            The "sending" service
	 * @param group
	 *            The group
	 * @param message
	 *            The message
	 */
	void incognito_send(RID fromService, RID group, GrowableBuffer message);

	/**
	 * Serialize and send a broadcast message on behalf of another service. This method is
	 * intended to be used by services that are able to connect multiple routers
	 * into a single environment.
	 * 
	 * @param fromService
	 *            The "sending" service
	 * @param group
	 *            The group
	 * @param message
	 *            The unserialized message
	 * @param serializer
	 *            The serializer will serialize the message
	 */
	<T> void incognito_send(RID fromService, RID group, T message, Serializer<T> serializer);
	
	
	/**
	 * Add a broadcast message consumer that will receive messages for any group 
	 * that <b>passes through the local router</b>.
	 * 
	 * @param consumer
	 *            The message consumer
	 */
	void incognito_addMessageConsumer(Consumer<BroadcastMessage<GrowableBuffer>> consumer);
	
	/**
	 * Remove an incognito message consumer
	 * 
	 * @param consumer
	 *            The message consumer
	 */
	void incognito_removeMessageConsumer(Consumer<BroadcastMessage<GrowableBuffer>> consumer);
}
