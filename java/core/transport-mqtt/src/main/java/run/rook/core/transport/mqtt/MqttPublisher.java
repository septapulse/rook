package run.rook.core.transport.mqtt;

import org.fusesource.mqtt.client.BlockingConnection;
import org.fusesource.mqtt.client.QoS;

import run.rook.api.exception.ExceptionHandler;
import run.rook.api.transport.GrowableBuffer;
import run.rook.api.transport.simple.SerializingPublisher;

/**
 * 
 * @author Eric Thill
 *
 */
class MqttPublisher extends SerializingPublisher {

	private final BlockingConnection conn;
	private final String topic;
	private final ExceptionHandler exceptionHandler;
	
	public MqttPublisher(BlockingConnection conn, String topic, ExceptionHandler exceptionHandler) {
		this.conn = conn;
		this.topic = topic;
		this.exceptionHandler = exceptionHandler;
	}

	@Override
	protected void send(GrowableBuffer writeBuf) {
		try {
			byte[] payload = new byte[writeBuf.length()];
			// FIXME change to an underlying MQTT library that doesn't require a payload copy of the exact size
			System.arraycopy(writeBuf.bytes(), 0, payload, 0, payload.length);
			conn.publish(topic, payload, QoS.EXACTLY_ONCE, false);
		} catch(Throwable t) {
			if(exceptionHandler != null) {
				exceptionHandler.error("MQTT Publish Exception", t);
			}
		}
	}
}
