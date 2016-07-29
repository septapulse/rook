package rook.core.io.service.raspberrypi.grovepi;

import java.io.IOException;

import rook.api.RID;
import rook.api.exception.InitException;
import rook.core.io.proxy.message.Cap;
import rook.core.io.proxy.message.CapType;
import rook.core.io.proxy.message.DataType;
import rook.core.io.proxy.message.IOValue;
import rook.core.io.service.IOOutput;
import rook.core.io.service.raspberrypi.grovepi.GrovePiPlusHardware.PinMode;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
public class GrovePiPlusAnalogOutput implements IOOutput {

	private final byte pin;
	private final RID id;
	private final GrovePiPlusHardware hw;
	
	public GrovePiPlusAnalogOutput(byte pin, RID id, GrovePiPlusHardware hw) {
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
			hw.analogWrite(pin, (byte)value.getValueAsInt());
		}
	}
	
	@Override
	public RID id() {
		return id;
	}
	
	@Override
	public Cap cap() {
		return new Cap().setCapType(CapType.OUTPUT).setDataType(DataType.INTEGER).setID(id)
				.setIncrement(1).setMinValue(0).setMaxValue(255);
	}

}
