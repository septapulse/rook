package io.septapulse.rook.core.io.raspberrypi.grovepi;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import io.septapulse.rook.core.io.raspberrypi.util.ThrottledI2CDevice;

/**
 * Hardware interface to the GrovePi+
 * 
 * @author Eric Thill
 *
 */
public class GrovePiPlusHardware {

	private static final byte UNUSED = 0;
	private static final byte DIGITAL_READ = 1;
	private static final byte DIGITAL_WRITE = 2;
	private static final byte ANALOG_READ = 3;
	private static final byte ANALOG_WRITE = 4;
	private static final byte PIN_MODE = 5;
	private static final byte VERSION = 8;
	
	private static final byte TRUE = 1;
	private static final byte FALSE = 0;

	public static final byte DEFAULT_BUS = I2CBus.BUS_1;
	public static final byte DEFAULT_ADDRESS = 0x04;

	private final ThrottledI2CDevice device;
	private final byte[] buffer = new byte[4];

	public GrovePiPlusHardware() throws IOException {
		this(I2CFactory.getInstance(I2CBus.BUS_1).getDevice(DEFAULT_ADDRESS));
	}
	
	public GrovePiPlusHardware(I2CDevice device) {
		this(new ThrottledI2CDevice(device, 20, true));
	}
	
	public GrovePiPlusHardware(ThrottledI2CDevice device) {
		this.device = device;
	}
	
	public ThrottledI2CDevice getDevice() {
		return device;
	}

	public String getFirmwareVersion() throws IOException {
		byte[] buffer = writeAndRead(VERSION, UNUSED, UNUSED, UNUSED);
		return buffer[1] + "." + buffer[2] + "." + buffer[3];
	}

	/**
	 * Reads boolean values from D* (digital) pins
	 * 
	 * @param pin
	 *            D(pin)
	 * @return
	 * @throws IOException
	 */
	public boolean digitalRead(byte pin) throws IOException {
		byte[] buffer = writeAndRead(DIGITAL_READ, pin, UNUSED, UNUSED);
		return buffer[0] > 0;
	}

	/**
	 * Writes boolean values to D* (digital) pins
	 * 
	 * @param pin
	 *            D(pin)
	 * @param value
	 * @throws IOException
	 */
	public void digitalWrite(byte pin, boolean value) throws IOException {
		write(DIGITAL_WRITE, pin, value ? TRUE : FALSE, UNUSED);
	}

	/**
	 * Reads from A* (analog) pins. A* (analog) pins are always inputs
	 * 
	 * @param pin
	 *            A(pin)
	 * @throws IOException
	 */
	public int analogRead(byte pin) throws IOException {
		byte[] buffer = writeAndRead(ANALOG_READ, pin, UNUSED, UNUSED);
		return buffer[1] * 256 + buffer[2];
	}

	/**
	 * Writes PWM values to PWM supported D* (digital) pins (D3,D5,D6)
	 * 
	 * @param pin
	 *            D(pin)
	 * @param value
	 *            value to rea
	 * @throws IOException
	 */
	public void analogWrite(byte pin, byte value) throws IOException {
		switch (pin) {
		case 3:
		case 5:
		case 6:
			write(ANALOG_WRITE, pin, value, UNUSED);
			break;
		default:
			throw new IOException("Cannot write analog value to non-PWM pin");
		}
	}

	/**
	 * Sets D* (digital) pin modes
	 * 
	 * @param pin
	 *            D(pin)
	 * @param mode
	 *            Input or Output
	 * @throws IOException
	 */
	public void pinMode(byte pin, PinMode mode) throws IOException {
		write(PIN_MODE, pin, mode.value(), UNUSED);
	}

	private void write(byte b1, byte b2, byte b3, byte b4) throws IOException {
		buffer[0] = b1;
		buffer[1] = b2;
		buffer[2] = b3;
		buffer[3] = b4;
		device.write(buffer, 0, buffer.length);
	}

	private byte[] writeAndRead(byte b1, byte b2, byte b3, byte b4) throws IOException {
		write(b1, b2, b3, b4);
		device.read(buffer, 0, buffer.length);
		return buffer;
	}

	public enum PinMode {
		INPUT((byte) 0), 
		OUTPUT((byte) 1); 
		private byte v;

		private PinMode(byte v) {
			this.v = v;
		}

		public byte value() {
			return v;
		}
	}
}
