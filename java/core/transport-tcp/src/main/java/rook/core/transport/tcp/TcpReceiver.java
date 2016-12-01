package rook.core.transport.tcp;

import java.io.DataInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rook.api.exception.ExceptionHandler;
import rook.api.transport.GrowableBuffer;
import rook.api.transport.simple.DeserializingDispatcher;
import rook.api.transport.simple.SimpleAnnounceTransport;
import rook.api.transport.simple.SimpleBroadcastTransport;
import rook.api.transport.simple.SimpleUnicastTransport;

/**
 * 
 * @author Eric Thill
 *
 */
class TcpReceiver {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final GrowableBuffer buf = GrowableBuffer.allocate(1024);
	private final DeserializingDispatcher dispatcher;
	private final DataInputStream in;
	private final ExceptionHandler exceptionHandler;
	private volatile boolean run;
	
	public TcpReceiver(DataInputStream in,
			SimpleAnnounceTransport announce, 
			SimpleBroadcastTransport bcast,
			SimpleUnicastTransport ucast,
			ExceptionHandler exceptionHandler) {
		this.in = in;
		this.exceptionHandler = exceptionHandler;
		dispatcher = new DeserializingDispatcher(announce, bcast, ucast);
	}

	public void start() throws Exception {
		run = true;
		new Thread(this::receiveLoop, getClass().getSimpleName()).start();
	}
	
	public void stop() {
		run = false;
	}
	
	private void receiveLoop() {
		while(run) {
			try {
				buf.reserve(4, false);
				in.readFully(buf.bytes(), 0, 4);
				buf.length(4);
				
				int len = buf.direct().getInt(0);
				
				buf.reserve(len, false);
				in.readFully(buf.bytes(), 0, len);
				buf.length(len);

				dispatcher.dispatch(buf.direct(), 0, len);
			} catch(Exception e) {
				stop();
				if(run) {
					if(exceptionHandler != null) {
						exceptionHandler.error("TCP Receive Error", e);
					} else {
						logger.error("TcpReceiver Error", e);
					}
				}
			}
		}
	}
	
}
