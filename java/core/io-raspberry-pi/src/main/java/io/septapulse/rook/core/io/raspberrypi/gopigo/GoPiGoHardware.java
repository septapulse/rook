package io.septapulse.rook.core.io.raspberrypi.gopigo;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import io.septapulse.rook.core.io.raspberrypi.util.ThrottledI2CDevice;

/**
 * Hardware interface to a GoPiGo
 * 
 * @author Eric Thill
 *
 */
public class GoPiGoHardware {
	private static final byte M1 = 111;
	private static final byte M2 = 112;
	private static final byte VOLT = 118;
	private static final byte US = 117;
	private static final byte SERVO = 101;
	private static final byte ENC_TGT = 50;
	private static final byte FW_VER = 20;
	private static final byte EN_SERVO = 61;
	private static final byte DIS_SERVO = 60;
	private static final byte ENC_READ = 53;
	private static final byte DIGITAL_WRITE = 12;
	private static final byte PIN_MODE = 16;
	private static final byte UNUSED = 0;

	private static final byte MOTOR_FORWARD = 1;
	private static final byte MOTOR_BACKWARD = 0;
	private static final byte ENCODER_RIGHT = 1;
	private static final byte ENCODER_LEFT = 0;
	private static final byte OUTPUT = 1;
	private static final byte OFF = 0;
	private static final byte ON = 1;

	public static final byte DEFAULT_BUS = I2CBus.BUS_1;
	public static final byte DEFAULT_ADDRESS = 0x08;

	private final ThrottledI2CDevice device;
	private final byte[] buffer = new byte[4];
	
	private int ultrasonicPin = 15;	
	
	public GoPiGoHardware() throws IOException {
		this(I2CFactory.getInstance(I2CBus.BUS_1).getDevice(DEFAULT_ADDRESS));
	}

	public GoPiGoHardware(I2CDevice device) throws IOException {
		this(new ThrottledI2CDevice(device, 20, true));
	}
	
	public GoPiGoHardware(ThrottledI2CDevice device) throws IOException {
		this.device = device;
	}

	public ThrottledI2CDevice getDevice() {
		return device;
	}

	public float readFirmwareVersion() throws IOException {
		writeI2C(FW_VER, UNUSED, UNUSED, UNUSED);
		byte[] buffer = readI2C(1);
		return (float) buffer[0] / 10;
	}

	public void writeLeftMotor(int value) throws IOException {
		writeMotor(M2, value);
	}

	public void writeRightMotor(int value) throws IOException {
		writeMotor(M1, value);
	}

	private void writeMotor(byte command, int value) throws IOException {
		// -255 to 255
		byte direction = value < 0 ? MOTOR_BACKWARD : MOTOR_FORWARD;
		int speed = Math.abs(value);
		if (speed > 255)
			speed = 255;
		writeI2C(command, direction, (byte) speed, UNUSED);
	}

	public void writeLeftLed(boolean value) throws IOException {
		writeI2C(PIN_MODE, (byte)17, OUTPUT, UNUSED);
		writeI2C(DIGITAL_WRITE, (byte)17, value ? ON : OFF, UNUSED);
	}

	public void writeRightLed(boolean value) throws IOException {
		writeI2C(PIN_MODE, (byte)16, OUTPUT, UNUSED);
		writeI2C(DIGITAL_WRITE, (byte)16, value ? ON : OFF, UNUSED);
	}
	
	public double readVoltage() throws IOException {
		writeI2C(VOLT, UNUSED, UNUSED, UNUSED);
		int high = readI2C(1)[0];
	    int low = readI2C(1)[0];
		double value = ((high & 0xFF) << 8) | (low & 0xFF);
		value = (5 * (float) value / 1024) / .4;
		value = Math.round(value * 100.0) / 100.0;
		return value;
	}

	public void writeServoEnabled(boolean enabled) throws IOException {
		if (enabled) {
			writeI2C(EN_SERVO, UNUSED, UNUSED, UNUSED);
		} else {
			writeI2C(DIS_SERVO, UNUSED, UNUSED, UNUSED);
		}
	}

	public void writeServoPosition(int value) throws IOException {
		// 0 to 255
		writeI2C(SERVO, (byte) value, UNUSED, UNUSED);
	}

	public void setUltrasonicPin(int ultrasonicPin) {
		this.ultrasonicPin = ultrasonicPin;
	}

	public int readUltrasonicDistance() throws IOException {
		writeI2C(US, (byte) ultrasonicPin, UNUSED, UNUSED);
		int high = readI2C(1)[0];
	    int low = readI2C(1)[0];
		return ((high & 0xFF) << 8) | (low & 0xFF);
	}

	// public byte[] readInfraredDistance() throws IOException {
	// // FIXME separate Thread
	// writeI2C(IR_RECV_PIN, UNUSED, UNUSED, UNUSED);
	// writeI2C(IR_READ, UNUSED, UNUSED, UNUSED);
	//
	// int b1 = (int)buffer[1] & 0xFF;
	// if (b1 != 255) {
	// return Arrays.copyOfRange(buffer, 1, buffer.length);
	// } else {
	// return null;
	// }
	// }
	
	public void writeEncoderTarget(boolean rightEnabled, boolean leftEnabled, int target) throws IOException {
	    byte m_sel = (byte)((rightEnabled?1:0) * 2 + (leftEnabled?1:0));
	    writeI2C(ENC_TGT, m_sel, (byte)((target >> 8) & 0xFF), (byte)(target & 0xFF));
	}
	
	public int readLeftEncoder() throws IOException {
		return readEncoder(ENCODER_LEFT);
	}
	
	public int readRightEncoder() throws IOException {
		return readEncoder(ENCODER_RIGHT);
	}
	
	private int readEncoder(byte motor) throws IOException {
		writeI2C(ENC_READ, motor, UNUSED, UNUSED);
		int high = readI2C(1)[0];
		int low = readI2C(1)[0];
		return ((high & 0xFF) << 8) | (low & 0xFF);
	}

	private void writeI2C(byte b1, byte b2, byte b3, byte b4) throws IOException {
		buffer[0] = b1;
		buffer[1] = b2;
		buffer[2] = b3;
		buffer[3] = b4;
		device.write(buffer, 0, 4);
	}

	private byte[] readI2C(int len) throws IOException {
		device.read(buffer, 0, len);
		return buffer;
	}
	
	public enum PinMode {
		INPUT((byte) 0), OUTPUT((byte) 1);
		private byte v;

		private PinMode(byte v) {
			this.v = v;
		}

		public byte value() {
			return v;
		}
	}
}
