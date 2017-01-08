package io.septapulse.rook.core.io.raspberrypi;

import java.util.List;

import io.septapulse.rook.api.config.Configurable;
import io.septapulse.rook.core.io.raspberrypi.gopigo.GoPiGoConfig;
import io.septapulse.rook.core.io.raspberrypi.gpio.GpioConfig;
import io.septapulse.rook.core.io.raspberrypi.grovepi.GrovePiPlusConfig;
import io.septapulse.rook.core.io.raspberrypi.hc_sr04.HC_SR04Config;

/**
 * Configuration for a {@link RaspberryPiService} {@link IOService}
 * 
 * @author Eric Thill
 *
 */
public class RaspberryPiConfig {
	
	@Configurable(min="1", increment="1", comment="Number of milliseconds between broadcasting Input/Output changes", defaultValue="100")
	public long broadcastInterval = 100;
	
	@Configurable(comment="GoPiGo")
	public GoPiGoConfig goPiGo;
	
	@Configurable(comment="GPIO")
	public GpioConfig gpio;

	@Configurable(comment="GrovePi")
	public GrovePiPlusConfig grovePi;

	@Configurable(comment="HC_SR04 connected to GPIO pins")
	public List<HC_SR04Config> hc_sr04;

}
