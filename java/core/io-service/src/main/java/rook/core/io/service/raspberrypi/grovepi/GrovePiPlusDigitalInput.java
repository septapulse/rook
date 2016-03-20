package rook.core.io.service.raspberrypi.grovepi;

import java.io.IOException;

import rook.api.InitException;
import rook.api.RID;
import rook.core.io.proxy.message.Cap;
import rook.core.io.proxy.message.CapType;
import rook.core.io.proxy.message.DataType;
import rook.core.io.proxy.message.IOValue;
import rook.core.io.service.IOInput;
import rook.core.io.service.raspberrypi.grovepi.GrovePiPlusHardware.PinMode;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
public class GrovePiPlusDigitalInput implements IOInput {

	private final byte pin;
	private final RID id;
	private final GrovePiPlusHardware hw;
	private final IOValue value = new IOValue();
	
	public GrovePiPlusDigitalInput(byte pin, RID id, GrovePiPlusHardware hw) {
		this.pin = pin;
		this.id = id;
		this.hw = hw;
	}
	
	@Override
	public void init() throws InitException {
		try {
			hw.pinMode(pin, PinMode.INPUT);
		} catch (IOException e) {
			throw new InitException(e);
		}
	}

	@Override
	public void shutdown() {

	}

	@Override
	public IOValue read() throws IOException {
		value.setID(id);
		value.setValue(hw.digitalRead(pin));
		return value;
	}

	@Override
	public Cap cap() {
		return new Cap().setCapType(CapType.INPUT).setDataType(DataType.BOOLEAN).setID(id);
	}

}
