package rook.core.io.service.raspberrypi.grovepi;

import rook.api.config.Configurable;
import rook.api.config.ConfigurableInteger;

/**
 * A configuration for a {@link GrovePiPlus}
 * 
 * @author Eric Thill
 *
 */
public class GrovePiPlusConfig {
	@ConfigurableInteger(comment = "I2C Bus")
	private Byte bus;
	@ConfigurableInteger(comment = "I2C Address")
	private Byte address;
	@Configurable(comment = "Pin A0")
	private AnalogPin A0;
	@Configurable(comment = "Pin A1")
	private AnalogPin A1;
	@Configurable(comment = "Pin A2")
	private AnalogPin A2;
	@Configurable(comment = "Pin D2")
	private DigitalPin D2;
	@Configurable(comment = "Pin D3")
	private DigitalPin D3;
	@Configurable(comment = "Pin D4")
	private DigitalPin D4;
	@Configurable(comment = "Pin D5")
	private DigitalPin D5;
	@Configurable(comment = "Pin D6")
	private DigitalPin D6;
	@Configurable(comment = "Pin D7")
	private DigitalPin D7;
	@Configurable(comment = "Pin D8")
	private DigitalPin D8;

	public Byte getBus() {
		return bus;
	}

	public Byte getAddress() {
		return address;
	}

	public AnalogPin getA0() {
		return A0;
	}

	public AnalogPin getA1() {
		return A1;
	}

	public AnalogPin getA2() {
		return A2;
	}

	public DigitalPin getD2() {
		return D2;
	}

	public DigitalPin getD3() {
		return D3;
	}

	public DigitalPin getD4() {
		return D4;
	}

	public DigitalPin getD5() {
		return D5;
	}

	public DigitalPin getD6() {
		return D6;
	}

	public DigitalPin getD7() {
		return D7;
	}

	public DigitalPin getD8() {
		return D8;
	}

	@Override
	public String toString() {
		return "GrovePiPlusConfig [bus=" + bus + ", address=" + address + ", A0=" + A0 + ", A1=" + A1 + ", A2=" + A2
				+ ", D2=" + D2 + ", D3=" + D3 + ", D4=" + D4 + ", D5=" + D5 + ", D6=" + D6 + ", D7=" + D7 + ", D8=" + D8
				+ "]";
	}
	
	public static class AnalogPin {
		@Configurable(comment = "The pin's Rook ID (Alphanumeric, Upper-case)")
		private String pinName;
		
		public String getPinName() {
			return pinName;
		}

		@Override
		public String toString() {
			return "AnalogPin [pinName=" + pinName + "]";
		}
	}

	public static class DigitalPin {
		@Configurable(comment = "The pin's Rook ID (Alphanumeric, Upper-case)")
		private String pinName;
		@Configurable(comment = "The pin's mode")
		private DigitalPinMode pinMode;
		@ConfigurableInteger(min=0, max=255, increment=1, 
				comment = "The pin's value to set when the system is shutdown (for OUTPUT and PWM only)")
		private Integer shutdownValue;
		
		public String getPinName() {
			return pinName;
		}
		
		public DigitalPinMode getPinMode() {
			return pinMode;
		}
		
		public Integer getShutdownValue() {
			return shutdownValue;
		}

		@Override
		public String toString() {
			return "DigitalPin [pinName=" + pinName + ", pinMode=" + pinMode + ", shutdownValue=" + shutdownValue + "]";
		}
	}

	public enum DigitalPinMode {
		OUTPUT, INPUT, PWM;
	}

}
