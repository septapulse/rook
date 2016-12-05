package io.septapulse.rook.core.io.service.raspberrypi.grovepi;

import java.io.IOException;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.exception.InitException;
import io.septapulse.rook.core.io.proxy.message.Cap;
import io.septapulse.rook.core.io.proxy.message.CapType;
import io.septapulse.rook.core.io.proxy.message.DataType;
import io.septapulse.rook.core.io.proxy.message.IOValue;
import io.septapulse.rook.core.io.service.IOInput;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
public class GrovePiPlusAnalogInput implements IOInput {

	private final byte pin;
	private final RID id;
	private final GrovePiPlusHardware hw;
	private final IOValue value = new IOValue();
	
	public GrovePiPlusAnalogInput(byte pin, RID id, GrovePiPlusHardware hw) {
		this.pin = pin;
		this.id = id.immutable();
		this.hw = hw;
	}
	
	@Override
	public void init() throws InitException {

	}

	@Override
	public void shutdown() {

	}

	@Override
	public IOValue read() throws IOException {
		int v;
		synchronized (hw) {
			v = hw.analogRead(pin);
		}
		return value.setValue(v);
	}
	
	@Override
	public RID id() {
		return id;
	}

	@Override
	public Cap cap() {
		return new Cap().setCapType(CapType.INPUT).setDataType(DataType.INTEGER).setID(id)
				.setIncrement(1).setMaxValue(1023).setMinValue(0);
	}

}
