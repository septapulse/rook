package run.rook.core.io.raspberrypi.gpio;

import java.util.List;

import run.rook.api.config.Configurable;

/**
 * A configuration for a {@link Gpio}
 * 
 * @author Eric Thill
 *
 */
public class GpioConfig {
	
	@Configurable(min="1", increment="1", comment="Number of milliseconds between broadcasting Input/Output changes", defaultValue="100")
	public long broadcastInterval = 100;
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
