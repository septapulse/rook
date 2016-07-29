package rook.api.transport.consumer;

import rook.api.RID;

public interface UnicastMessageConsumer<T> {
	/**
	 * Consume Unicast Messages
	 * 
	 * @param from
	 *            The service ID that this message was addressed from
	 * @param to
	 *            The service ID that this message was addressed to
	 * @param payload
	 *            The message payload being sent
	 */
	void onUnicastMessage(RID from, RID to, T payload);
}
