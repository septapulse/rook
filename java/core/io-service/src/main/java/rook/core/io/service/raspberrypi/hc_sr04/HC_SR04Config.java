package rook.core.io.service.raspberrypi.hc_sr04;

import rook.api.config.Configurable;

/**
 * A configuration for a {@link HC_SR04}
 * 
 * @author Eric Thill
 *
 */
public class HC_SR04Config {
	@Configurable(comment = "Unique RID")
	public String id;
	@Configurable(comment = "Raspberry Pi Pin used for 'trig'")
	public String trigPin;
	@Configurable(comment = "Raspberry Pi Pin used for 'echo'")
	public String echoPin;
	@Configurable(comment = "Round distance to integer value")
	public boolean convertToInteger = false;
	@Configurable(comment = "Minimum distance that can be represented (Default: 0)")
	public double minDistance = 0;
	@Configurable(comment = "Maximun distance that can be represented (Default: 1000)")
	public double maxDinstance = 1000.0;
	
	@Override
	public String toString() {
		return "HC_SR04Config [id=" + id + ", trigPin=" + trigPin + ", echoPin=" + echoPin + ", convertToInteger="
				+ convertToInteger + ", minDistance=" + minDistance + ", maxDinstance=" + maxDinstance + "]";
	}
}
