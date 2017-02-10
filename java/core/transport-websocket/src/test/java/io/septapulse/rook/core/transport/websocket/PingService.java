package io.septapulse.rook.core.transport.websocket;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.Service;
import io.septapulse.rook.api.ServiceLauncher;
import io.septapulse.rook.api.exception.InitException;
import io.septapulse.rook.api.transport.GrowableBuffer;
import io.septapulse.rook.api.transport.Transport;
import io.septapulse.rook.core.transport.websocket.WebsocketTransport;

public class PingService implements Service {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private Transport transport;
	
	@Override
	public void setTransport(Transport transport) {
		this.transport = transport;
	}

	@Override
	public void init() throws InitException {
		transport.ucast().addMessageConsumer(this::onUnicast);
		logger.info("Initialized");
	}
	
	private void onUnicast(RID from, RID to, GrowableBuffer data) {
		logger.info("Received from " + from + ": " 
				+ new String(Arrays.copyOf(data.bytes(), data.length())));
		transport.ucast().send(from, data);
	}

	@Override
	public void shutdown() {

	}
	
	public static void main(String[] args) {
		ServiceLauncher.main(
				"-id", "PING",
				"-st", PingService.class.getName(), 
				"-sc", "{}",
				"-tt", WebsocketTransport.class.getName(), 
				"-tc", "{}" 
				);
	}

}
