package io.septapulse.rook.core.io.service.raspberrypi.gpio;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.core.io.proxy.message.Cap;
import io.septapulse.rook.core.io.proxy.message.CapType;
import io.septapulse.rook.core.io.proxy.message.DataType;
import io.septapulse.rook.core.io.proxy.message.IOValue;
import io.septapulse.rook.core.io.service.IOOutput;

/**
 * A GPIO Digital Output
 * 
 * @author Eric Thill
 *
 */
public class GpioDigitalOutput implements IOOutput {

	private final Cap cap;
	private final Pin pin;
	private final PinState shutdownState;
	private GpioPinDigitalOutput output;
	
	public GpioDigitalOutput(String pin, RID id, boolean shutdownState) {
		this.pin = RaspiPin.getPinByName(pin);
		this.shutdownState = shutdownState ? PinState.HIGH : PinState.LOW;
		this.cap = new Cap().setCapType(CapType.OUTPUT).setDataType(DataType.BOOLEAN).setID(id.immutable());
	}
	
	@Override
	public void init() {
		output = GpioFactory.getInstance().provisionDigitalOutputPin(pin);
		if(shutdownState != null) {
			output.setShutdownOptions(true, shutdownState);
		}
	}

	@Override
	public void write(IOValue value) {
		output.setState(value.getValueAsBoolean());
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
		if(shutdownState != null) {
			output.setState(shutdownState);
		}
	}
}
