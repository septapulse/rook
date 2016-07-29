package rook.core.io.service.raspberrypi;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rook.api.config.Configurable;
import rook.api.exception.InitException;
import rook.core.io.service.IOService;
import rook.core.io.service.raspberrypi.gopigo.GoPiGo;
import rook.core.io.service.raspberrypi.gpio.Gpio;
import rook.core.io.service.raspberrypi.grovepi.GrovePiPlus;
import rook.core.io.service.raspberrypi.hc_sr04.HC_SR04;
import rook.core.io.service.raspberrypi.hc_sr04.HC_SR04Config;

/**
 * An {@link IOService} implementation that can communicate with a {@link RaspberryPi}
 * 
 * @author Eric Thill
 *
 */
public class RaspberryPi extends IOService {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final List<RaspberryPiDevice> devices = new ArrayList<>();
	
	@Configurable
	public RaspberryPi(RaspberryPiConfig config) throws InitException {
		super(DEFAULT_BROADCAST_INTERVAL, DEFAULT_RECONNECT_INTERVAL);
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
				device.init(ioManager);
			}
			logger.info("Waiting 2 seconds for configuration to complete");
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onShutdown() {
		
	}

}
