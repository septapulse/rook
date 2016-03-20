package rook.core.io.service.raspberrypi;

import java.util.List;

import rook.api.config.Configurable;
import rook.core.io.service.IOService;
import rook.core.io.service.raspberrypi.gopigo.GoPiGoConfig;
import rook.core.io.service.raspberrypi.gpio.GpioConfig;
import rook.core.io.service.raspberrypi.grovepi.GrovePiPlusConfig;
import rook.core.io.service.raspberrypi.hc_sr04.HC_SR04Config;

/**
 * Configuration for a {@link RaspberryPi} {@link IOService}
 * 
 * @author Eric Thill
 *
 */
public class RaspberryPiConfig {
	
	@Configurable(comment="GoPiGo")
	public GoPiGoConfig goPiGo;
	
	@Configurable(comment="GPIO")
	public GpioConfig gpio;

	@Configurable(comment="GrovePi")
	public GrovePiPlusConfig grovePi;

	@Configurable(comment="HC_SR04 connected to GPIO pins")
	public List<HC_SR04Config> hc_sr04;

}
