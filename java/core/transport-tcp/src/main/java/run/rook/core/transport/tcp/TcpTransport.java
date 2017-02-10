package run.rook.core.transport.tcp;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import run.rook.api.RID;
import run.rook.api.config.Configurable;
import run.rook.api.exception.ExceptionHandler;
import run.rook.api.exception.InitException;
import run.rook.api.transport.AnnounceTransport;
import run.rook.api.transport.BroadcastTransport;
import run.rook.api.transport.ControllableTransport;
import run.rook.api.transport.Transport;
import run.rook.api.transport.UnicastTransport;
import run.rook.api.transport.simple.SimpleAnnounceTransport;
import run.rook.api.transport.simple.SimpleBroadcastTransport;
import run.rook.api.transport.simple.SimpleUnicastTransport;

/**
 * A {@link Transport} implementation that uses a {@link Socket} to communicate
 * with a {@link TcpRouter}
 * 
 * @author Eric Thill
 *
 */
public class TcpTransport implements ControllableTransport {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final String host;
	private final int port;
	private Socket sock;
	private TcpPublisher publisher;
	private TcpReceiver receiver;
	private SimpleAnnounceTransport announceTransport;
	private SimpleBroadcastTransport bcastTransport;
	private SimpleUnicastTransport ucastTransport;
	private RID serviceId;
	private ExceptionHandler exceptionHandler;
	private boolean respondToProbes = true;

	@Configurable
	public TcpTransport(TcpTransportConfig config) {
		host = config.getHost();
		port = config.getPort();
	}

	@Override
	public void setServiceId(RID serviceId) {
		this.serviceId = serviceId;
	}

	@Override
	public void setExceptionHandler(ExceptionHandler h) {
		this.exceptionHandler = h;
	}

	@Override
	public void setRespondToProbes(boolean respondToProbes) {
		this.respondToProbes = respondToProbes;
	}

	@Override
	public synchronized void start() throws InitException {
		if (running.compareAndSet(false, true)) {
			try {
				logger.info("Connecting to " + host + ":" + port);
				sock = new Socket(host, port);
				logger.info("Connected");
				publisher = new TcpPublisher(sock.getOutputStream(), exceptionHandler);
				announceTransport = new SimpleAnnounceTransport(serviceId, publisher, respondToProbes);
				bcastTransport = new SimpleBroadcastTransport(serviceId, publisher);
				ucastTransport = new SimpleUnicastTransport(serviceId, publisher, null);
				receiver = new TcpReceiver(new DataInputStream(sock.getInputStream()), announceTransport,
						bcastTransport, ucastTransport, exceptionHandler);
				receiver.start();
				if (respondToProbes)
					announceTransport.incognito_announce(serviceId);
				logger.info("Started");
			} catch (Throwable t) {
				throw new InitException("Could not start transport", t);
			}
		}
	}

	@Override
	public synchronized void shutdown() {
		if (running.compareAndSet(true, false)) {
			try {
				sock.close();
			} catch (IOException e) {
				// we tried
			}
		}
	}

	@Override
	public AnnounceTransport announce() {
		return announceTransport;
	}

	@Override
	public BroadcastTransport bcast() {
		return bcastTransport;
	}

	@Override
	public UnicastTransport ucast() {
		return ucastTransport;
	}

}
