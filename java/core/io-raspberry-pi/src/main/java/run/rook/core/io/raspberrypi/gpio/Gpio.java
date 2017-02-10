package run.rook.core.io.raspberrypi.gpio;

import run.rook.api.RID;
import run.rook.api.exception.InitException;
import run.rook.core.io.raspberrypi.RaspberryPiDevice;
import run.rook.core.io.service.IOService;

/**
 * A {@link RaspberryPiDevice} that can interface with GPIO pins
 * 
 * @author Eric Thill
 *
 */
public class Gpio implements RaspberryPiDevice {

	private final GpioConfig config;
	
	public Gpio(GpioConfig config) {
		this.config = config;
	}
	
	@Override
	public void init(IOService ioManager) throws InitException {
		for(GpioConfig.PinConfig c : config.pins) {
			RID id = RID.create(c.name);
			if(c.type == GpioConfig.PinType.INPUT) {
				ioManager.addInput(new GpioDigitalInput(c.pin, id));
			} else if(c.type == GpioConfig.PinType.OUTPUT) {
				ioManager.addOutput(new GpioDigitalOutput(c.pin, id, c.shutdownState));
				// TODO implement: new IOValue().setValue(c.shutdownState);
			}
		}
	}

	@Override
	public void shutdown() {
		
	}

	@Override
	public String toString() {
		return "Gpio [config=" + config + "]";
	}

}
