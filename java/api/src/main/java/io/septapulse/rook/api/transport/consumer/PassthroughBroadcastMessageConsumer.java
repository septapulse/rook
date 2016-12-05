package io.septapulse.rook.api.transport.consumer;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.transport.GrowableBuffer;

/**
 * Passes the incoming {@link BroadcastMessage} to the underlying base consumer
 * 
 * @author Eric Thill
 *
 */
public class PassthroughBroadcastMessageConsumer implements ProxyBroadcastMessageConsumer<GrowableBuffer> {

	private final BroadcastMessageConsumer<GrowableBuffer> consumer;

	public PassthroughBroadcastMessageConsumer(BroadcastMessageConsumer<GrowableBuffer> consumer) {
		this.consumer = consumer;
	}
	
	@Override
	public void onBroadcastMessage(RID from, RID group, GrowableBuffer payload) {
		consumer.onBroadcastMessage(from, group, payload);
	}
	
	@Override
	public BroadcastMessageConsumer<GrowableBuffer> getBaseConsumer() {
		return consumer;
	}
}
