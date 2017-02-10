package run.rook.api.transport.consumer;

import run.rook.api.RID;

public interface BroadcastMessageConsumer<T> {
	/**
	 * Consume Broadcast Message Events
	 * 
	 * @param from
	 *            The service ID that this message was addressed from
	 * @param group
	 *            The group the message is being sent on
	 * @param payload
	 *            The message payload that was sent
	 */
	void onBroadcastMessage(RID from, RID group, T payload);
}