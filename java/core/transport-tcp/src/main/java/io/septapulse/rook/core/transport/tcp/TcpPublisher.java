package io.septapulse.rook.core.transport.tcp;

import java.io.OutputStream;

import io.septapulse.rook.api.exception.ExceptionHandler;
import io.septapulse.rook.api.transport.GrowableBuffer;
import io.septapulse.rook.api.transport.simple.SerializingPublisher;

/**
 * 
 * @author Eric Thill
 *
 */
class TcpPublisher extends SerializingPublisher {

	private final GrowableBuffer lengthBuffer = GrowableBuffer.allocate(4).length(4);
	private final OutputStream out;
	private final ExceptionHandler exceptionHandler;
	
	public TcpPublisher(OutputStream out, ExceptionHandler exceptionHandler) {
		this.out = out;
		this.exceptionHandler = exceptionHandler;
	}

	@Override
	protected void send(GrowableBuffer writeBuf) {
		try {
			if(writeBuf.length() == 0)
				Thread.dumpStack();
			lengthBuffer.reserve(4, false);
			lengthBuffer.direct().putInt(0, writeBuf.length());
			lengthBuffer.length(4);
			
			out.write(lengthBuffer.bytes(), 0, lengthBuffer.length());
			out.write(writeBuf.bytes(), 0, writeBuf.length());
		} catch(Throwable t) {
			if(exceptionHandler != null) {
				exceptionHandler.error("TCP Publish Exception", t);
			}
		}
	}
}
