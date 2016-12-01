package rook.core.transport.aeron;

import io.aeron.Publication;
import rook.api.transport.GrowableBuffer;
import rook.api.transport.simple.SerializingPublisher;

/**
 * 
 * @author Eric Thill
 *
 */
class AeronPublisher extends SerializingPublisher {

	private final Publication pub;
	
	public AeronPublisher(Publication pub) {
		this.pub = pub;
	}

	@Override
	protected void send(GrowableBuffer writeBuf) {
		while(pub.offer(writeBuf.direct(), 0, writeBuf.length()) < 0) {
			Thread.yield();
		}
	}
}
