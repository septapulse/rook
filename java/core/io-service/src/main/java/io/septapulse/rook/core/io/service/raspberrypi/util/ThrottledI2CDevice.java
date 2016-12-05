package io.septapulse.rook.core.io.service.raspberrypi.util;

import java.io.IOException;

import com.pi4j.io.i2c.I2CDevice;

import io.septapulse.rook.api.util.Sleep;

/**
 * I2C has baud limits. This throttles the messages being passed to the
 * underlying I2C device to help keep the baud rate from being exceeded.
 * 
 * @author Eric Thill
 *
 */
public class ThrottledI2CDevice {

	private final I2CDevice device;
	private final long throttleIntervalMillis;
	private final boolean synchronize;
	private volatile long lastThrottleTime;

	public ThrottledI2CDevice(I2CDevice device, long throttleIntervalMillis, boolean synchronize) {
		this.device = device;
		this.throttleIntervalMillis = throttleIntervalMillis;
		this.synchronize = synchronize;
	}

	public void write(byte[] buf, int off, int len) throws IOException {
		if (synchronize) {
			synchronized (this) {
				throttle();
				device.write(buf, off, len);
			}
		} else {
			throttle();
			device.write(buf, off, len);
		}
	}

	public void read(byte[] buf, int off, int len) throws IOException {
		if (synchronize) {
			synchronized (this) {
				throttle();
				device.read(buf, off, len);
			}
		} else {
			throttle();
			device.read(buf, off, len);
		}
	}

	public void writeAndRead(byte[] writeBuf, int writeOff, int writeLen, byte[] readBuf, int readOff, int readLen)
			throws IOException {
		if (synchronize) {
			synchronized (this) {
				throttle();
				device.write(writeBuf, writeOff, writeLen);
				device.read(readBuf, readOff, readLen);
			}
		} else {
			throttle();
			device.write(writeBuf, writeOff, writeLen);
			device.read(readBuf, readOff, readLen);
		}
	}

	private void throttle() {
		long sleepTime = throttleIntervalMillis-(System.currentTimeMillis() - lastThrottleTime);
		if(sleepTime > 0) {
			Sleep.trySleep(sleepTime);
		}
		lastThrottleTime = System.currentTimeMillis();
	}
}
