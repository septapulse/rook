package rook.core.io.service.raspberrypi.gpio;

import java.io.IOException;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import rook.api.RID;
import rook.core.io.proxy.message.Cap;
import rook.core.io.proxy.message.CapType;
import rook.core.io.proxy.message.DataType;
import rook.core.io.proxy.message.IOValue;
import rook.core.io.service.IOInput;

/**
 * A GPIO Digital Input
 * 
 * @author Eric Thill
 *
 */
public class GpioDigitalInput implements IOInput {

	private final IOValue value = new IOValue();
	private final Cap cap;
	private final Pin pin;
	private GpioPinDigitalInput input;
	
	public GpioDigitalInput(String pin, RID id) {
		this.pin = RaspiPin.getPinByName(pin);
		this.cap = new Cap().setCapType(CapType.INPUT).setDataType(DataType.BOOLEAN).setID(id.immutable());
	}
	
	@Override
	public void init() {
		input = GpioFactory.getInstance().provisionDigitalInputPin(pin);
	}

	@Override
	public IOValue read() throws IOException {
		value.setValue(input.getState() == PinState.HIGH ? true : false);
		return value;
	}
	
	@Override
	public RID id() {
		return cap.getId();
	}

	@Override
	public Cap cap() {
		return cap;
	}

	@Override
	public void shutdown() {
		
	}

	
}
