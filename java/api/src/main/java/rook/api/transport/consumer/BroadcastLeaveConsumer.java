package rook.api.transport.consumer;

import rook.api.RID;

public interface BroadcastLeaveConsumer {
	/**
	 * Consume Broadcast Leave Events
	 * 
	 * @param from
	 *            the leaving service ID
	 * @param group
	 *            the group the service is leaving
	 */
	void onBroadcastLeave(RID from, RID group);
}
