package run.rook.core.io.raspberrypi.grovepi;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import run.rook.api.RID;
import run.rook.api.exception.InitException;
import run.rook.core.io.raspberrypi.RaspberryPiDevice;
import run.rook.core.io.raspberrypi.grovepi.GrovePiPlusConfig.DigitalPinMode;
import run.rook.core.io.raspberrypi.util.ThrottledI2CDevice;
import run.rook.core.io.service.IOService;

/**
 * A {@link RaspberryPiDevice} for Dexter Industries GrovePi+
 * 
 * @author Eric Thill
 *
 */
public class GrovePiPlus implements RaspberryPiDevice {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final GrovePiPlusConfig config;
	
	public GrovePiPlus(GrovePiPlusConfig config) throws InitException {
		this.config = config;
	}
	
	@Override
	public void init(IOService ioManager) throws InitException {
		logger.info("Initializing GrovePiPlus Device");
		try {
			byte bus = config.getBus() != null ? config.getBus() : GrovePiPlusHardware.DEFAULT_BUS;
			byte address = config.getAddress() != null ? config.getAddress() : GrovePiPlusHardware.DEFAULT_ADDRESS;
			I2CDevice device = I2CFactory.getInstance(bus).getDevice(address);
			ThrottledI2CDevice throttledDevice = new ThrottledI2CDevice(device, config.getI2cThrottleIntervalMillis(), false);
			GrovePiPlusHardware hw = new GrovePiPlusHardware(throttledDevice);
			logger.info("GrovePiPlus firmware version: " + hw.getFirmwareVersion());
			addAnalogPin(ioManager, hw, 0, config.getA0());
			addAnalogPin(ioManager, hw, 1, config.getA1());
			addAnalogPin(ioManager, hw, 2, config.getA2());
			addDigitalPin(ioManager, hw, 2, config.getD2());
			addDigitalPin(ioManager, hw, 3, config.getD3());
			addDigitalPin(ioManager, hw, 4, config.getD4());
			addDigitalPin(ioManager, hw, 5, config.getD5());
			addDigitalPin(ioManager, hw, 6, config.getD6());
			addDigitalPin(ioManager, hw, 7, config.getD7());
			addDigitalPin(ioManager, hw, 8, config.getD8());
		} catch(IOException e) {
			if(e.getMessage().contains("Cannot open file handle for /dev/i2c")) {
				throw new InitException("It appears I2C is not enabled. To enable, try: sudo raspi-config -> 'Advanced Options' -> 'I2C' -> '[Yes]'");
			} else {
				throw new InitException("Could not start GrovePiPlus " + config.toString(), e);
			}
		} catch(Throwable t) {
			throw new InitException("Could not start GrovePiPlus " + config.toString(), t);
		}
		logger.info("GrovePiPlus Device initialized");
	}
	
	private void addAnalogPin(IOService ioManager, GrovePiPlusHardware hw, int pin, GrovePiPlusConfig.AnalogPin cfg) {
		if(cfg != null && cfg.getPinName() != null && cfg.getPinName().length() > 0) {
			String pinName = cfg.getPinName();
			RID id = RID.create(pinName);
			logger.info("Initializing A" + pin + " - ID=" + id);
			ioManager.addInput(new GrovePiPlusAnalogInput((byte)pin, id, hw));
		}
	}
	
	private void addDigitalPin(IOService ioManager, GrovePiPlusHardware hw, int pin, GrovePiPlusConfig.DigitalPin cfg) {
		if(cfg != null && cfg.getPinName() != null && cfg.getPinName().length() > 0 && cfg.getPinMode() != null) {
			RID id = RID.create(cfg.getPinName());
			GrovePiPlusConfig.DigitalPinMode pinMode = cfg.getPinMode();
			// TODO implement
//			IOValue shutdownValue = cfg.getShutdownValue() != null ?
//					new IOValue().setValue(cfg.getShutdownValue()) : null;
			logger.info("Initializing D" + pin + " - ID=" + id + " PinMode=" + pinMode);
			if(pinMode == DigitalPinMode.INPUT) {
				ioManager.addInput(new GrovePiPlusDigitalInput((byte)pin, id, hw));
			} else if(pinMode == DigitalPinMode.OUTPUT) {
				ioManager.addOutput(new GrovePiPlusDigitalOutput((byte)pin, id, hw));
			} else if(pinMode == DigitalPinMode.PWM) {
				ioManager.addOutput(new GrovePiPlusAnalogOutput((byte)pin, id, hw));
			} else {
				throw new IllegalArgumentException("Invalid pinMode '" + pinMode + "' for D" + 
						pin + " cfg=" + cfg + " - Valid pinMode values: INPUT,OUTPUT,PWM");
			}
		}
	}

	@Override
	public void shutdown() {
		// nothing to do
	}

	@Override
	public String toString() {
		return "GrovePiPlus [config=" + config + "]";
	}

}
