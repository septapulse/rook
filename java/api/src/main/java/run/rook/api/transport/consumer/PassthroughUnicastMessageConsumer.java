package run.rook.api.transport.consumer;

import run.rook.api.RID;
import run.rook.api.transport.GrowableBuffer;

/**
 * Passes the incoming {@link UnicastMessage} to the underlying base consumer
 * 
 * @author Eric Thill
 *
 */
public class PassthroughUnicastMessageConsumer implements ProxyUnicastMessageConsumer<GrowableBuffer> {

	private final UnicastMessageConsumer<GrowableBuffer> consumer;

	public PassthroughUnicastMessageConsumer(UnicastMessageConsumer<GrowableBuffer> consumer) {
		this.consumer = consumer;
	}
	
	@Override
	public void onUnicastMessage(RID from, RID to, GrowableBuffer payload) {
		consumer.onUnicastMessage(from, to, payload);
	}
	
	@Override
	public UnicastMessageConsumer<GrowableBuffer> getBaseConsumer() {
		return consumer;
	}
}
