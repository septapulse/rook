package rook.core.mqtt.service;

import java.util.Arrays;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import rook.api.proxy.ProxySender;
import rook.api.proxy.ProxyService;

/**
 * MQTT {@link ProxySender} used by {@link ProxyService}
 * 
 * @author Eric Thill
 *
 */
public class MqttSender extends ProxySender {

	private MqttClient client;
	
	public void setClient(MqttClient client) {
		this.client = client;
	}
	
	@Override
	public void send(byte[] message, int length) throws Exception {
		if(client != null) {
			if(message.length != length) {
				message = Arrays.copyOf(message, length);
			}
			MqttMessage sendMessage = new MqttMessage(message);
			client.publish(MqttConstants.ROOK, sendMessage);
		}
	}
	
}
