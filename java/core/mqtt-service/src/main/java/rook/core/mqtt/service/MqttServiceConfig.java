package rook.core.mqtt.service;

import rook.api.config.Configurable;
import rook.api.config.ConfigurableInteger;

/**
 * Configuration for a {@link MqttService}
 * 
 * @author Eric Thill
 *
 */
public class MqttServiceConfig {
	@Configurable(comment="The MQTT Server URI")
	private String serverURI;
	@Configurable(comment="The MQTT Client ID")
	private String clientId;
	@ConfigurableInteger(min=1, max=Integer.MAX_VALUE, increment=1, comment="The default payload size")
	private Integer payloadSize;
	@Configurable(comment="If a connection to the MQTT broker is required upon startup. When set to true, the entire local environment will not start if the broker connection cannot be made. Default is true.")
	private boolean startupConnectionRequired = true;
	
	public String getClientId() {
		return clientId;
	}
	
	public String getServerURI() {
		return serverURI;
	}
	
	public Integer getPayloadSize() {
		return payloadSize;
	}
	
	public boolean isStartupConnectionRequired() {
		return startupConnectionRequired;
	}
}
