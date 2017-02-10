package run.rook.core.io.raspberrypi.gopigo;

import run.rook.api.config.Configurable;

/**
 * A configuration for a {@link GoPiGo}
 * 
 * @author Eric Thill
 *
 */
public class GoPiGoConfig {
	@Configurable(min="1", increment="1", comment="Number of milliseconds between broadcasting Input/Output changes", defaultValue="100")
	private long broadcastInterval = 100;
	@Configurable(comment = "I2C Bus", defaultValue="1")
	private Byte bus = 1;
	@Configurable(comment = "I2C Address", defaultValue="8")
	private Byte address = 8;
	@Configurable(comment = "Left Encoder Input ID", defaultValue = "SPEED_LEFT")
	private String encoderLeft = "SPEED_LEFT";
	@Configurable(comment = "Right Encoder Input ID", defaultValue = "SPEED_RIGHT")
	private String encoderRight = "SPEED_RIGHT";
	@Configurable(comment = "Left LED Output ID", defaultValue = "LED_LEFT")
	private String ledLeft = "LED_LEFT";
	@Configurable(comment = "Right LED Output ID", defaultValue = "LED_RIGHT")
	private String ledRight = "LED_RIGHT";
	@Configurable(comment = "Left Motor Output ID", defaultValue = "MOTOR_LEFT")
	private String motorLeft = "MOTOR_LEFT";
	@Configurable(comment = "Right Motor Output ID", defaultValue = "MOTOR_RIGHT")
	private String motorRight = "MOTOR_RIGHT";
	@Configurable(comment = "Servo Position Output ID", defaultValue = "SERVO")
	private String servoPosition = "SERVO";
	@Configurable(comment = "Servo Enabled Output ID", defaultValue = "SERVO_POWER")
	private String servoEnabled = "SERVO_POWER";
	@Configurable(comment = "Ultrasonic Distance Input ID", defaultValue = "DISTANCE")
	private String ultrasonicDistance = "DISTANCE";
	@Configurable(comment = "Voltage Input ID", defaultValue = "")
	private String voltage = "VOLTAGE";
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
