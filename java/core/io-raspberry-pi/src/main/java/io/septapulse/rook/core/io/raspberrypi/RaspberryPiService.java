package io.septapulse.rook.core.io.raspberrypi;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.septapulse.rook.api.config.Configurable;
import io.septapulse.rook.api.exception.InitException;
import io.septapulse.rook.core.io.raspberrypi.gopigo.GoPiGo;
import io.septapulse.rook.core.io.raspberrypi.gpio.Gpio;
import io.septapulse.rook.core.io.raspberrypi.grovepi.GrovePiPlus;
import io.septapulse.rook.core.io.raspberrypi.hc_sr04.HC_SR04;
import io.septapulse.rook.core.io.raspberrypi.hc_sr04.HC_SR04Config;
import io.septapulse.rook.core.io.service.IOService;

/**
 * An {@link IOService} implementation that can communicate with a {@link RaspberryPiService}
 * 
 * @author Eric Thill
 *
 */
public class RaspberryPiService extends IOService {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final List<RaspberryPiDevice> devices = new ArrayList<>();
	
	@Configurable
	public RaspberryPiService(RaspberryPiConfig config) throws InitException {
		super(config.broadcastInterval);
		logger.info("config: " + config);
		if(config.goPiGo != null)
			devices.add(new GoPiGo(config.goPiGo));
		if(config.gpio != null)
			devices.add(new Gpio(config.gpio));
		if(config.grovePi != null)
			devices.add(new GrovePiPlus(config.grovePi));
		if(config.hc_sr04 != null)
			for(HC_SR04Config c : config.hc_sr04)
				devices.add(new HC_SR04(c));
	}
	
	@Override
	public void onInit() throws InitException {
		try {
			for(RaspberryPiDevice device : devices) {
				logger.info("Initializing " + device);
				device.init(this);
			}
			logger.info("Waiting 2 seconds for configuration to complete");
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onShutdown() {
		for(RaspberryPiDevice device : devices) {
			logger.info("Shutdown " + device);
			device.shutdown();
		}
	}

}
