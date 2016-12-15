package io.septapulse.rook.core.io.raspberrypi.gopigo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.septapulse.rook.api.config.Configurable;
import io.septapulse.rook.api.exception.InitException;
import io.septapulse.rook.core.io.raspberrypi.RaspberryPiDevice;
import io.septapulse.rook.core.io.service.IOService;

public class GoPiGoService extends IOService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final RaspberryPiDevice device;
	
	@Configurable
	public GoPiGoService(GoPiGoConfig config) throws InitException {
		super(DEFAULT_BROADCAST_INTERVAL, DEFAULT_RECONNECT_INTERVAL);
		logger.info("config: " + config);
		device = new GoPiGo(config);
	}
	
	@Override
	public void onInit() throws InitException {
		try {
			logger.info("Initializing " + device);
			device.init(ioManager);
			logger.info("Waiting 2 seconds for configuration to complete");
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onShutdown() {
		logger.info("Shutdown " + device);
		device.shutdown();
	}

}
