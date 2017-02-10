package run.rook.core.transport.mqtt;

import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.Message;
import org.fusesource.mqtt.client.QoS;
import org.fusesource.mqtt.client.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import run.rook.api.exception.ExceptionHandler;
import run.rook.api.transport.simple.DeserializingDispatcher;
import run.rook.api.transport.simple.SimpleAnnounceTransport;
import run.rook.api.transport.simple.SimpleBroadcastTransport;
import run.rook.api.transport.simple.SimpleUnicastTransport;

/**
 * 
 * @author Eric Thill
 *
 */
class MqttReceiver {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final MutableDirectBuffer directBuffer = new UnsafeBuffer(new byte[0]);
	private final DeserializingDispatcher dispatcher;
	private final BlockingConnection conn;
	private final String topic;
	private final ExceptionHandler exceptionHandler;
	private volatile boolean run;
	
	public MqttReceiver(BlockingConnection conn,
			String topic,
			SimpleAnnounceTransport announce, 
			SimpleBroadcastTransport bcast,
			SimpleUnicastTransport ucast,
			ExceptionHandler exceptionHandler) {
		this.conn = conn;
		this.topic = topic;
		this.exceptionHandler = exceptionHandler;
		dispatcher = new DeserializingDispatcher(announce, bcast, ucast);
	}

	public void start() throws Exception {
		run = true;
		conn.subscribe(new Topic[] { new Topic(topic, QoS.EXACTLY_ONCE) });
		new Thread(this::receiveLoop, getClass().getSimpleName()).start();
	}
	
	public void stop() {
		run = false;
	}
	
	private void receiveLoop() {
		while(run) {
			try {
				Message m = conn.receive();
				byte[] bytes = m.getPayload();
				directBuffer.wrap(bytes);
				dispatcher.dispatch(directBuffer, 0, bytes.length);
			} catch(Exception e) {
				if(run) {
					stop();
					if(exceptionHandler != null) {
						exceptionHandler.error("MQTT Receive Error", e);
					} else {
						logger.error("MQTT Receiver Error", e);
					}
				}
			}
		}
	}
	
}
