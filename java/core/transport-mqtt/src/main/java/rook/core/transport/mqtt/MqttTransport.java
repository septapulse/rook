package rook.core.transport.mqtt;

import java.util.concurrent.atomic.AtomicBoolean;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.MQTT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rook.api.RID;
import rook.api.config.Configurable;
import rook.api.exception.ExceptionHandler;
import rook.api.exception.InitException;
import rook.api.transport.AnnounceTransport;
import rook.api.transport.BroadcastTransport;
import rook.api.transport.ControllableTransport;
import rook.api.transport.Transport;
import rook.api.transport.UnicastTransport;
import rook.api.transport.simple.SimpleAnnounceTransport;
import rook.api.transport.simple.SimpleBroadcastTransport;
import rook.api.transport.simple.SimpleUnicastTransport;

/**
 * A {@link Transport} implementation that communicates with an MQTT broker.
 * 
 * @author Eric Thill
 *
 */
public class MqttTransport implements ControllableTransport {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final String host;
	private final int port;
	private final String topic;
	private BlockingConnection conn;
	private MqttPublisher publisher;
	private MqttReceiver receiver;
	private SimpleAnnounceTransport announceTransport;
	private SimpleBroadcastTransport bcastTransport;
	private SimpleUnicastTransport ucastTransport;
	private RID serviceId;
	private ExceptionHandler exceptionHandler;
	private boolean respondToProbes = true;

	@Configurable
	public MqttTransport(MqttTransportConfig config) {
		host = config.getBrokerHost();
		port = config.getBrokerPort();
		topic = config.getTopic();
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
				MQTT mqtt = new MQTT();
				mqtt.setHost(host, port);
				conn = mqtt.blockingConnection();
				conn.connect();
				logger.info("Connected");
				publisher = new MqttPublisher(conn, topic, exceptionHandler);
				announceTransport = new SimpleAnnounceTransport(serviceId, publisher, respondToProbes);
				bcastTransport = new SimpleBroadcastTransport(serviceId, publisher);
				ucastTransport = new SimpleUnicastTransport(serviceId, publisher, null);
				receiver = new MqttReceiver(conn, topic, announceTransport, bcastTransport, ucastTransport,
						exceptionHandler);
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
				conn.disconnect();
			} catch (Exception e) {
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
