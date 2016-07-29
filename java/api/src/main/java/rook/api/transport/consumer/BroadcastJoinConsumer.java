package rook.api.transport.consumer;

import rook.api.RID;

public interface BroadcastJoinConsumer {
	/**
	 * Consume BroadcastJoin Events
	 * 
	 * @param from
	 *            the joining service ID
	 * @param group
	 *            the group the service is joining
	 */
	void onBroadcastJoin(RID from, RID group);
}
