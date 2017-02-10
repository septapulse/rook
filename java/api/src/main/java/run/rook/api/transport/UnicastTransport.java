package run.rook.api.transport;

import run.rook.api.RID;
import run.rook.api.transport.consumer.UnicastMessageConsumer;

/**
 * Transport for unicast messages. Unicast messages are sent directly to an
 * explicit service.
 * 
 * @author Eric Thill
 *
 */
public interface UnicastTransport {

	/**
	 * Add a unicast message consumer that will receive messages addressed to
	 * this service from all other services.
	 * 
	 * @param consumer
	 *            The message consumer
	 */
	void addMessageConsumer(UnicastMessageConsumer<GrowableBuffer> consumer);

	/**
	 * Add a deserializing unicast message consumer that will receive messages
	 * addressed to this service from all other services.
	 * 
	 * @param consumer
	 *            The message consumer
	 * @param deserializer
	 *            The deserializer that converts a binary message into an object
	 *            to be consumed
	 */
	<T> void addMessageConsumer(UnicastMessageConsumer<T> consumer, Deserializer<T> deserializer);

	/**
	 * Add a unicast message consumer that will receive messages addressed to
	 * this service from the given service.
	 * 
	 * @param from
	 *            The service to listen to. Only messages from this service ID
	 *            will be passed to the consumer.
	 * @param consumer
	 *            The message consumer
	 */
	void addMessageConsumer(RID from, UnicastMessageConsumer<GrowableBuffer> consumer);

	/**
	 * Add a deserializing unicast message consumer that will receive messages
	 * addressed to this service from the given service.
	 * 
	 * @param from
	 *            The service to listen to. Only messages from this service ID
	 *            will be passed to the consumer.
	 * @param consumer
	 *            The message consumer
	 * @param deserializer
	 *            The deserializer that converts a binary message into an object
	 *            to be consumed
	 */
	<T> void addMessageConsumer(RID from, UnicastMessageConsumer<T> consumer, Deserializer<T> deserializer);

	/**
	 * Remove a consumer. The given consumer will stop receiving message
	 * callbacks.
	 * 
	 * @param consumer
	 *            The consumer to remove
	 */
	<T> void removeMessageConsumer(UnicastMessageConsumer<T> consumer);

	/**
	 * Send a unicast message to the given service
	 * 
	 * @param toService
	 *            The service to send the message to
	 * @param message
	 *            The message
	 */
	void send(RID toService, GrowableBuffer message);

	/**
	 * Serialize and send a unicast message to the given service
	 * 
	 * @param toService
	 *            The service to send the message to
	 * @param message
	 *            The unserialized message
	 * @param serializer
	 *            The serializer that will serialize the message
	 */
	<T> void send(RID toService, T message, Serializer<T> serializer);

	/**
	 * Add a unicast message consumer that will receive messages addressed to
	 * any service that <b>passes through the local router</b>.
	 * 
	 * @param consumer
	 *            The message consumer
	 */
	void incognito_addMessageConsumer(UnicastMessageConsumer<GrowableBuffer> consumer);

	/**
	 * Add a deserializing unicast message consumer that will receive messages
	 * addressed to any service that <b>passes through the local router</b>.
	 * 
	 * @param consumer
	 *            The message consumer
	 * @param deserializer
	 *            The deserializer that converts a binary message into an object
	 *            to be consumed
	 */
	<T> void incognito_addMessageConsumer(UnicastMessageConsumer<T> consumer, Deserializer<T> deserializer);

	/**
	 * Remove an incognito consumer. The given consumer will stop receiving
	 * message callbacks.
	 * 
	 * @param consumer
	 *            The consumer to remove
	 */
	<T> void incognito_removeMessageConsumer(UnicastMessageConsumer<T> consumer);

	/**
	 * Send a unicast message as any service. This method is intended to be used
	 * by services that are able to connect multiple routers into a single
	 * environment.
	 * 
	 * @param fromService
	 *            The "sending" service
	 * @param toService
	 *            The service to send the message to
	 * @param message
	 *            The message
	 */
	void incognito_send(RID fromService, RID toService, GrowableBuffer message);

	/**
	 * Serialize and send a unicast message as any service. This method is
	 * intended to be used by services that are able to connect multiple routers
	 * into a single environment.
	 * 
	 * @param fromService
	 *            The "sending" service
	 * @param toService
	 *            The service to send the message to
	 * @param message
	 *            The unserialized message
	 * @param serializer
	 *            The serializer that will serialize the message
	 */
	<T> void incognito_send(RID fromService, RID toService, T message, Serializer<T> serializer);

}
