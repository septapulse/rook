package rook.core.mqtt.service;

import java.util.Arrays;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rook.api.InitException;
import rook.api.proxy.ProxySender;
import rook.api.proxy.ProxyService;

/**
 * A {@link ProxyService} that uses MQTT for the underlying middleware
 * 
 * @author Eric Thill
 *
 */
public class MqttService extends ProxyService {
	
	private static final int DEFAULT_PAYLOAD_SIZE = 4096;
	private static final int RETRY_INTERVAL = 5000;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final MqttServiceConfig cfg;
	private MqttClient client;
	private MqttSender sender;
	
	public MqttService(MqttServiceConfig cfg) {
		super(cfg.getPayloadSize() != null ? cfg.getPayloadSize() : DEFAULT_PAYLOAD_SIZE);
		this.cfg = cfg;
	}

	@Override
	public ProxySender initialize() throws InitException {
		logger.info("Initializing MQTT Service");
		sender = new MqttSender();
		try {
			connect();
		} catch(Throwable t) {
			if(cfg.isStartupConnectionRequired()) {
				throw new InitException("Could not initialize MqttService", t);
			} else {
				logger.error("Could not connect to Broker", t);
				new Thread(reconnect).start();
			}
		}
		logger.info("Initialized MQTT Service");
		return sender;
	}
	
	private void connect() throws MqttException {
		client = new MqttClient(cfg.getServerURI(), cfg.getClientId());
		client.setCallback(mqttCallback);
		client.connect();
		client.subscribe(new String[] { MqttConstants.ROOK });
		sender.setClient(client);
	}
	
	private final MqttCallback mqttCallback = new MqttCallback() {
		
		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			if(logger.isDebugEnabled()) {
				logger.debug("Received '" + topic + "': " + Arrays.toString(message.getPayload()));
			}
			if(MqttConstants.ROOK.equals(topic)) {
				handlePayload(message.getPayload(), 0, message.getPayload().length);
			}
		}
		
		@Override
		public void deliveryComplete(IMqttDeliveryToken token) {
			
		}
		
		@Override
		public void connectionLost(Throwable cause) {
			logger.error("Lost connection to Broker", cause);
			new Thread(reconnect).start();
		}
	};
	
	@Override
	public void shutdown() {
		if(client != null) {
			try {
				client.disconnect();
			} catch (Throwable t) {
				logger.error("Could not disconnect MQTT client", t);
			}
		}
	}
	
	private final Runnable reconnect = new Runnable() {
		@Override
		public void run() {
			long lastLog = System.currentTimeMillis();
			logger.info("Will retry Broker connection every " + RETRY_INTERVAL + " milliseconds...");
			boolean success = false;
			while(!success) {
				try {
					connect();
					logger.info("Connected to Broker!");
					success = true;
					// will trigger this service and every other ProxySender to send current state
					sender.sendResendRequest();
				} catch (MqttException e) {
					if(System.currentTimeMillis() - lastLog > 600000) {
						// every 10 minutes
						logger.error("Still trying to connect to Broker...");
						lastLog = System.currentTimeMillis();
					}
					try {
						Thread.sleep(RETRY_INTERVAL);
					} catch (InterruptedException e1) {

					}
				}
			}
		}
	};


}
