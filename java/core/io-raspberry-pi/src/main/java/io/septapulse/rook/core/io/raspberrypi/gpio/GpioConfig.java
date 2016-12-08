package io.septapulse.rook.core.io.raspberrypi.gpio;

import java.util.List;

/**
 * A configuration for a {@link Gpio}
 * 
 * @author Eric Thill
 *
 */
public class GpioConfig {
	
	public List<PinConfig> pins;
	
	public class PinConfig {
		public String name;
		public String pin;
		public PinType type;
		public boolean shutdownState;
		@Override
		public String toString() {
			return "PinConfig [name=" + name + ", pin=" + pin + ", type=" + type + ", shutdownState=" + shutdownState
					+ "]";
		}
	}
	
	@Override
	public String toString() {
		return "GpioConfig [pins=" + pins + "]";
	}
	
	public enum PinType {
		INPUT, OUTPUT;
	}

}
