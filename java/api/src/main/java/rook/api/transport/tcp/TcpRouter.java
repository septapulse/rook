package rook.api.transport.tcp;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rook.api.Router;
import rook.api.config.Configurable;
import rook.api.transport.GrowableBuffer;

/**
 * A {@link Router} that spins up a TCP Server/Broker
 * 
 * @author Eric Thill
 *
 */
public class TcpRouter implements Router {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Set<Session> sessions = Collections.synchronizedSet(new LinkedHashSet<>());
	private final int port;
	private final ServerSocket server;
	private volatile boolean run;
	
	@Configurable
	public TcpRouter(TcpRouterConfig config) throws IOException {
		this.port = config.getPort();
		server = new ServerSocket(port);
	}
	
	public synchronized void start() {
		if(!run) {
			run = true;
			new Thread(this::run, "TcpRouter-"+port).start();
		}
	}
	
	public synchronized void stop() {
		if(run) {
			run = false;
			try {
				server.close();
			} catch (IOException e) {
				// tried
			}
		}
	}
	
	public void run() {
		try {
			while(run) {
				Socket sock = server.accept();
				try {
					Session sess = new Session(sock);
					sessions.add(sess);
					sess.start();
				} catch(Throwable t) {
					logger.info("Could not start session " + sock);
				}
			}
		} catch(Throwable t) {
			if(run) {
				logger.error("TCP Server crashed", t);
			}
		}
	}
	
	public void dispatch(GrowableBuffer buf) {
		synchronized (sessions) {
			for(Session s : sessions) {
				s.send(buf);
			}
		}
	}
	
	private class Session {
		
		private final GrowableBuffer lengthBuf = GrowableBuffer.allocate(4);
		private final Socket sock;
		private final DataInputStream in;
		private final OutputStream out;
		private final BlockingQueue<GrowableBuffer> sendQueue = 
				new ArrayBlockingQueue<>(4096);
		private volatile boolean run = true;
		
		public Session(Socket sock) throws IOException {
			this.sock = sock;
			this.in = new DataInputStream(sock.getInputStream());
			this.out = sock.getOutputStream();
		}
		
		public void start() {
			new Thread(this::sendLoop, "RouterSender-"+sock).start();
			new Thread(this::readLoop, "RouterReader-"+sock).start();
			logger.info("Started");
		}
		
		public void stop() {
			run = false;
			sessions.remove(this);
		}
		
		private void sendLoop() {
			try {
				while(run) {
					GrowableBuffer buf = sendQueue.poll(1, TimeUnit.SECONDS);
					if(buf != null) {
						if(buf.length() == 0)
							Thread.dumpStack();
						lengthBuf.reserve(4, false);
						lengthBuf.direct().putInt(0, buf.length());
						lengthBuf.length(4);
						
						out.write(lengthBuf.bytes(), 0, lengthBuf.length());
						out.write(buf.bytes(), 0, buf.length());
					}
				}
			} catch(Throwable t) {
				stop();
			}
		}
		
		private void readLoop() {
			try {
				while(run) {
					
					GrowableBuffer buf = GrowableBuffer.allocate(64);
					in.readFully(buf.bytes(), 0, 4);
					buf.length(4);
					
					int len = buf.direct().getInt(0);
					
					buf.reserve(len, false);
					in.readFully(buf.bytes(), 0, len);
					buf.length(len);
					
					dispatch(buf);
				}
			} catch(Throwable t) {
				stop();
			}
		}
		
		public void send(GrowableBuffer buf) {
			if(!sendQueue.offer(buf)) {
				logger.warn(sock + " queue is full! Dropping message.");
			}
		}
	}
}
