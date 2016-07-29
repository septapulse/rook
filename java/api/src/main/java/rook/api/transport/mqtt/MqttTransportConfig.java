package rook.api.transport.mqtt;

import rook.api.config.Configurable;

/**
 * Parsed configuration for an {@link MqttTransport}
 * 
 * @author Eric Thill
 *
 */
public class MqttTransportConfig {
	@Configurable(comment="MQTT Broker Host", defaultValue="localhost")
	private String brokerHost = "localhost";
	@Configurable(comment="MQTT Broker Port", defaultValue="1883")
	private int brokerPort = 1883;
	@Configurable(comment="MQTT Topic", defaultValue="ROOK")
	private String topic = "ROOK";
	
	public String getBrokerHost() {
		return brokerHost;
	}
	
	public int getBrokerPort() {
		return brokerPort;
	}
	
	public String getTopic() {
		return topic;
	}
}
	
