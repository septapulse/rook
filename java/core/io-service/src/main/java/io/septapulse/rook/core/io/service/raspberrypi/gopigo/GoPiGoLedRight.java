package io.septapulse.rook.core.io.service.raspberrypi.gopigo;

import java.io.IOException;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.exception.InitException;
import io.septapulse.rook.core.io.proxy.message.Cap;
import io.septapulse.rook.core.io.proxy.message.CapType;
import io.septapulse.rook.core.io.proxy.message.DataType;
import io.septapulse.rook.core.io.proxy.message.IOValue;
import io.septapulse.rook.core.io.service.IOOutput;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
public class GoPiGoLedRight implements IOOutput {

	private final Cap cap = new Cap()
			.setCapType(CapType.OUTPUT)
			.setDataType(DataType.BOOLEAN)
			.setMinValue(0)
			.setMaxValue(1)
			.setIncrement(1);
	private final GoPiGoHardware hw;
	
	public GoPiGoLedRight(RID id, GoPiGoHardware hw) {
		this.hw = hw;
		this.cap.setID(id.immutable());
	}
	
	@Override
	public void init() throws InitException {

	}

	@Override
	public void shutdown() {

	}

	@Override
	public void write(IOValue value) throws IOException {
		synchronized (hw) {
			hw.writeRightLed(value.getValueAsBoolean());
		}
	}
	
	@Override
	public RID id() {
		return cap.getId();
	}

	@Override
	public Cap cap() {
		return cap;
	}

}
