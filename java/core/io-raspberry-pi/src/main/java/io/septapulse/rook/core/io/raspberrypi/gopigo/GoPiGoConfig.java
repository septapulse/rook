package io.septapulse.rook.core.io.raspberrypi.gopigo;

import io.septapulse.rook.api.config.Configurable;

/**
 * A configuration for a {@link GoPiGo}
 * 
 * @author Eric Thill
 *
 */
public class GoPiGoConfig {
	@Configurable(min="1", increment="1", comment="Number of milliseconds between broadcasting Input/Output changes", defaultValue="100")
	private long broadcastInterval = 100;
	@Configurable(comment = "I2C Bus")
	private Byte bus;
	@Configurable(comment = "I2C Address")
	private Byte address;
	@Configurable(comment = "Left Encoder Input ID")
	private String encoderLeft;
	@Configurable(comment = "Right Encoder Input ID")
	private String encoderRight;
	@Configurable(comment = "Left LED Output ID")
	private String ledLeft;
	@Configurable(comment = "Right LED Output ID")
	private String ledRight;
	@Configurable(comment = "Left Motor Output ID")
	private String motorLeft;
	@Configurable(comment = "Right Motor Output ID")
	private String motorRight;
	@Configurable(comment = "Servo Position Output ID")
	private String servoPosition;
	@Configurable(comment = "Servo Enabled Output ID")
	private String servoEnabled;
	@Configurable(comment = "Ultrasonic Distance Input ID")
	private String ultrasonicDistance;
	@Configurable(comment = "Voltage Input ID")
	private String voltage;
	@Configurable(comment = "I2C Throttle Interval Millis", defaultValue = "20")
	private long i2cThrottleIntervalMillis = 20;

	public long getBroadcastInterval() {
		return broadcastInterval;
	}
	
	public Byte getBus() {
		return bus;
	}

	public Byte getAddress() {
		return address;
	}

	public String getEncoderLeft() {
		return encoderLeft;
	}

	public String getEncoderRight() {
		return encoderRight;
	}

	public String getLedLeft() {
		return ledLeft;
	}

	public String getLedRight() {
		return ledRight;
	}

	public String getMotorLeft() {
		return motorLeft;
	}

	public String getMotorRight() {
		return motorRight;
	}

	public String getServoPosition() {
		return servoPosition;
	}

	public String getServoEnabled() {
		return servoEnabled;
	}

	public String getUltrasonicDistance() {
		return ultrasonicDistance;
	}

	public String getVoltage() {
		return voltage;
	}

	public long getI2cThrottleIntervalMillis() {
		return i2cThrottleIntervalMillis;
	}

	@Override
	public String toString() {
		return "GoPiGoConfig [bus=" + bus + ", address=" + address + ", encoderLeft=" + encoderLeft + ", encoderRight="
				+ encoderRight + ", ledLeft=" + ledLeft + ", ledRight=" + ledRight + ", motorLeft=" + motorLeft
				+ ", motorRight=" + motorRight + ", servoPosition=" + servoPosition + ", servoEnabled=" + servoEnabled
				+ ", ultrasonicDistance=" + ultrasonicDistance + ", voltage=" + voltage + ", i2cThrottleIntervalMillis="
				+ i2cThrottleIntervalMillis + "]";
	}

}
