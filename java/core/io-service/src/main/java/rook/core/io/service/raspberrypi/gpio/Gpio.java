package rook.core.io.service.raspberrypi.gpio;

import rook.api.RID;
import rook.api.exception.InitException;
import rook.core.io.proxy.message.IOValue;
import rook.core.io.service.IOManager;
import rook.core.io.service.raspberrypi.RaspberryPiDevice;

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
	public void init(IOManager ioManager) throws InitException {
		for(GpioConfig.PinConfig c : config.pins) {
			RID id = RID.create(c.name);
			if(c.type == GpioConfig.PinType.INPUT) {
				ioManager.addInput(id, new GpioDigitalInput(c.pin, id));
			} else if(c.type == GpioConfig.PinType.OUTPUT) {
				ioManager.addOutput(id, new GpioDigitalOutput(c.pin, id, c.shutdownState), new IOValue().setValue(c.shutdownState));
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
