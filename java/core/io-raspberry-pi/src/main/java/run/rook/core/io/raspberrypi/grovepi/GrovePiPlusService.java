package run.rook.core.io.raspberrypi.grovepi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import run.rook.api.config.Configurable;
import run.rook.api.exception.InitException;
import run.rook.core.io.raspberrypi.RaspberryPiDevice;
import run.rook.core.io.service.IOService;

public class GrovePiPlusService extends IOService {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final RaspberryPiDevice device;
	
	@Configurable
	public GrovePiPlusService(GrovePiPlusConfig config) throws InitException {
		super(config.getBroadcastInterval());
		logger.info("config: " + config);
		device = new GrovePiPlus(config);
	}
	
	@Override
	public void onInit() throws InitException {
		try {
			logger.info("Initializing " + device);
			device.init(this);
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
