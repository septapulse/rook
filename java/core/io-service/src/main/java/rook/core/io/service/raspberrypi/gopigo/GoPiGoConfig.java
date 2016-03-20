package rook.core.io.service.raspberrypi.gopigo;

import rook.api.config.ConfigurableInteger;

/**
 * A configuration for a {@link GoPiGo}
 * 
 * @author Eric Thill
 *
 */
public class GoPiGoConfig {
	@ConfigurableInteger(comment = "I2C Bus")
	private Byte bus;
	@ConfigurableInteger(comment = "I2C Address")
	private Byte address;
	@ConfigurableInteger(comment = "Left Encoder Input ID")
	private String encoderLeft;
	@ConfigurableInteger(comment = "Right Encoder Input ID")
	private String encoderRight;
	@ConfigurableInteger(comment = "Left LED Output ID")
	private String ledLeft;
	@ConfigurableInteger(comment = "Right LED Output ID")
	private String ledRight;
	@ConfigurableInteger(comment = "Left Motor Output ID")
	private String motorLeft;
	@ConfigurableInteger(comment = "Right Motor Output ID")
	private String motorRight;
	@ConfigurableInteger(comment = "Servo Position Output ID")
	private String servoPosition;
	@ConfigurableInteger(comment = "Servo Enabled Output ID")
	private String servoEnabled;
	@ConfigurableInteger(comment = "Ultrasonic Distance Input ID")
	private String ultrasonicDistance;
	@ConfigurableInteger(comment = "Voltage Input ID")
	private String voltage;
	
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

	@Override
	public String toString() {
		return "GoPiGoConfig [bus=" + bus + ", address=" + address + ", encoderLeft=" + encoderLeft + ", encoderRight="
				+ encoderRight + ", ledLeft=" + ledLeft + ", ledRight=" + ledRight + ", motorLeft=" + motorLeft
				+ ", motorRight=" + motorRight + ", servoPosition=" + servoPosition + ", servoEnabled=" + servoEnabled
				+ ", ultrasonicDistance=" + ultrasonicDistance + ", voltage=" + voltage + "]";
	}
	
}
