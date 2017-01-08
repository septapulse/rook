package io.septapulse.rook.api.transport;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.transport.consumer.BroadcastJoinConsumer;
import io.septapulse.rook.api.transport.consumer.BroadcastLeaveConsumer;
import io.septapulse.rook.api.transport.consumer.BroadcastMessageConsumer;

/**
 * Transport for broadcast messages. Broadcast messages are sent once and
 * delivered to all services that are currently connected to the environment.
 * 
 * @author Eric Thill
 *
 */
public interface BroadcastTransport {

	/**
	 * Add a broadcast message consumer. This will not join the given group. Use
	 * the join(RID group) method to start receiving group messages.
	 * 
	 * @param consumer
	 *            The message consumer
	 */
	void addMessageConsumer(BroadcastMessageConsumer<GrowableBuffer> consumer);

	/**
	 * Add a broadcast message consumer for the given group from a specified
	 * service. This will not join the given group. Use the join(RID group)
	 * method to start receiving group messages.
	 * 
	 * @param group
	 *            Optional. Filters callbacks that are not for the given group.
	 * @param from
	 *            Optional. Filters callbacks that are not from the given
	 *            service id.
	 * @param consumer
	 *            The message consumer
	 */
	void addMessageConsumer(RID group, RID from, BroadcastMessageConsumer<GrowableBuffer> consumer);

	/**
	 * Add a deserializing broadcast message consumer for the given group from a
	 * specified service. This will not join the given group. Use the join(RID
	 * group) method to start receiving group messages.
	 * 
	 * @param group
	 *            Optional. Filters callbacks that are not for the given group.
	 * @param from
	 *            Optional. Filters callbacks that are not from the given
	 *            service id.
	 * @param consumer
	 *            The message consumer
	 * @param deserializer
	 *            The deserializer that converts a binary message into an object
	 *            to be consumed
	 */
	<T> void addMessageConsumer(RID group, RID from, BroadcastMessageConsumer<T> consumer,
			Deserializer<T> deserializer);

	/**
	 * Remove a broadcast message consumer.
	 * 
	 * @param consumer
	 *            The consumer to remove
	 */
	<T> void removeMessageConsumer(BroadcastMessageConsumer<T> consumer);

	/**
	 * Join the given broadcast group. Callbacks will not receive messages until
	 * a group is joined.
	 * 
	 * @param group
	 *            The group to join
	 * @return true if this was a new join, false if the transport already belonged to the group
	 */
	boolean join(RID group);

	/**
	 * Leave the given broadcast group. This will stop the flow of messages to
	 * callbacks for the given group.
	 * 
	 * @param group
	 *            The group to leave
	 * @param true if the group was left, false if the transport didn't belong to the group
	 */
	boolean leave(RID group);

	/**
	 * Add a consumer to listen to JOIN events
	 * 
	 * @param consumer
	 *            The consumer
	 */
	void addJoinConsumer(BroadcastJoinConsumer consumer);

	/**
	 * Remove a JOIN event consumer
	 * 
	 * @param consumer
	 *            The consumer
	 */
	void removeJoinConsumer(BroadcastJoinConsumer consumer);

	/**
	 * Add a consumer to listen to LEAVE events
	 * 
	 * @param consumer
	 *            The consumer
	 */
	void addLeaveConsumer(BroadcastLeaveConsumer consumer);

	/**
	 * Remove a LEAVE event consumer
	 * 
	 * @param consumer
	 *            The consumer
	 */
	void removeLeaveConsumer(BroadcastLeaveConsumer consumer);

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
	 * Serialize and send a broadcast message for the given group. This method
	 * is intended to be used by services that are able to connect multiple
	 * routers into a single environment.
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
	 * Send a JOIN event on behalf of another service. This method is intended
	 * to be used by services that are able to connect multiple routers into a
	 * single environment.
	 * 
	 * @param fromService
	 *            The "sending" service
	 * @param group
	 *            The group
	 */
	void incognito_join(RID fromService, RID group);

	/**
	 * Send a LEAVE event on behalf of another service. This method is intended
	 * to be used by services that are able to connect multiple routers into a
	 * single environment.
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
	 * Serialize and send a broadcast message on behalf of another service. This
	 * method is intended to be used by services that are able to connect
	 * multiple routers into a single environment.
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
	void incognito_addMessageConsumer(BroadcastMessageConsumer<GrowableBuffer> consumer);

	/**
	 * Remove an incognito message consumer
	 * 
	 * @param consumer
	 *            The message consumer
	 */
	void incognito_removeMessageConsumer(BroadcastMessageConsumer<GrowableBuffer> consumer);
}
