package rook.core.io.service.raspberrypi.gopigo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

import rook.api.RID;
import rook.api.exception.InitException;
import rook.core.io.proxy.message.IOValue;
import rook.core.io.service.IOInput;
import rook.core.io.service.IOManager;
import rook.core.io.service.IOOutput;
import rook.core.io.service.raspberrypi.RaspberryPiDevice;
import rook.core.io.service.raspberrypi.util.ThrottledI2CDevice;

/**
 * A {@link RaspberryPiDevice} for Dexter Industries GoPiGo
 * 
 * @author Eric Thill
 *
 */
public class GoPiGo implements RaspberryPiDevice {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final GoPiGoConfig config;
	
	public GoPiGo(GoPiGoConfig config) throws InitException {
		this.config = config;
	}
	
	@Override
	public void init(IOManager ioManager) throws InitException {
		logger.info("Initializing GoPiGo Device");
		try {
			byte bus = config.getBus() != null ? config.getBus() : GoPiGoHardware.DEFAULT_BUS;
			byte address = config.getAddress() != null ? config.getAddress() : GoPiGoHardware.DEFAULT_ADDRESS;
			I2CDevice device = I2CFactory.getInstance(bus).getDevice(address);
			ThrottledI2CDevice throttledDevice = new ThrottledI2CDevice(device, config.getI2cThrottleIntervalMillis(), false);
			GoPiGoHardware hw = new GoPiGoHardware(throttledDevice);
			logger.info("GoPiGo firmware version: " + hw.readFirmwareVersion());
			if(hasContent(config.getEncoderLeft()))
				add(ioManager, new GoPiGoEncoderLeft(RID.create(config.getEncoderLeft()).immutable(), hw));
			if(hasContent(config.getEncoderRight()))
				add(ioManager, new GoPiGoEncoderRight(RID.create(config.getEncoderRight()).immutable(), hw));
			if(hasContent(config.getLedLeft()))
				add(ioManager, new GoPiGoLedLeft(RID.create(config.getLedLeft()).immutable(), hw));
			if(hasContent(config.getLedRight()))
				add(ioManager, new GoPiGoLedRight(RID.create(config.getLedRight()).immutable(), hw));
			if(hasContent(config.getMotorLeft()))
				add(ioManager, new GoPiGoMotorLeft(RID.create(config.getMotorLeft()).immutable(), hw));
			if(hasContent(config.getMotorRight()))
				add(ioManager, new GoPiGoMotorRight(RID.create(config.getMotorRight()).immutable(), hw));
			if(hasContent(config.getServoEnabled()))
				add(ioManager, new GoPiGoServoEnabled(RID.create(config.getServoEnabled()).immutable(), hw));
			if(hasContent(config.getServoPosition()))
				add(ioManager, new GoPiGoServoPosition(RID.create(config.getServoPosition()).immutable(), hw));
			if(hasContent(config.getUltrasonicDistance()))
				add(ioManager, new GoPiGoUltrasonicDistance(RID.create(config.getUltrasonicDistance()).immutable(), hw));
			if(hasContent(config.getVoltage()))
				add(ioManager, new GoPiGoVoltage(RID.create(config.getVoltage()).immutable(), hw));
			
			// set encoder targets if they are enabled
			if(hasContent(config.getEncoderLeft()) || hasContent(config.getEncoderRight())) {
				hw.writeEncoderTarget(hasContent(config.getEncoderRight()), 
						hasContent(config.getEncoderLeft()), 18);
			}
			
			// if servo position is configured, but servo enabled is not, permanently enable the servo
			hw.writeServoEnabled(hasContent(config.getServoPosition()) && !hasContent(config.getServoEnabled()));
		} catch(Throwable t) {
			throw new InitException("Could not start GoPiGo " + config.toString(), t);
		}
		logger.info("GoPiGo Device initialized");
	}
	
	private boolean hasContent(String s) {
		return s != null && !s.isEmpty();
	}

	private void add(IOManager ioManager, IOInput input) {
		logger.info("Adding input: " + input.cap().getId());
		ioManager.addInput(input.cap().getId(), input);
	}
	
	private void add(IOManager ioManager, IOOutput output) {
		logger.info("Adding output: " + output.cap().getId());
		ioManager.addOutput(output.cap().getId(), output, new IOValue(0));
	}

	@Override
	public void shutdown() {
		// nothing to do
	}

	@Override
	public String toString() {
		return "GoPiGo [config=" + config + "]";
	}

}
