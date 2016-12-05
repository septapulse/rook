package io.septapulse.rook.core.io.service.raspberrypi.grovepi;

import java.io.IOException;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.exception.InitException;
import io.septapulse.rook.core.io.proxy.message.Cap;
import io.septapulse.rook.core.io.proxy.message.CapType;
import io.septapulse.rook.core.io.proxy.message.DataType;
import io.septapulse.rook.core.io.proxy.message.IOValue;
import io.septapulse.rook.core.io.service.IOOutput;
import io.septapulse.rook.core.io.service.raspberrypi.grovepi.GrovePiPlusHardware.PinMode;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
public class GrovePiPlusDigitalOutput implements IOOutput {

	private final byte pin;
	private final RID id;
	private final GrovePiPlusHardware hw;
	
	public GrovePiPlusDigitalOutput(byte pin, RID id, GrovePiPlusHardware hw) {
		this.pin = pin;
		this.id = id.immutable();
		this.hw = hw;
	}
	
	@Override
	public void init() throws InitException {
		try {
			hw.pinMode(pin, PinMode.OUTPUT);
		} catch (IOException e) {
			throw new InitException(e);
		}
	}

	@Override
	public void shutdown() {

	}

	@Override
	public void write(IOValue value) throws IOException {
		synchronized (hw) {
			hw.digitalWrite(pin, value.getValueAsBoolean());
		}
	}
	
	@Override
	public RID id() {
		return id;
	}
	
	@Override
	public Cap cap() {
		return new Cap().setCapType(CapType.OUTPUT).setDataType(DataType.BOOLEAN).setID(id);
	}

}
