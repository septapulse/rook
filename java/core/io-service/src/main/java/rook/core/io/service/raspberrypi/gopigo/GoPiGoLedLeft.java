package rook.core.io.service.raspberrypi.gopigo;

import java.io.IOException;

import rook.api.RID;
import rook.api.exception.InitException;
import rook.core.io.proxy.message.Cap;
import rook.core.io.proxy.message.CapType;
import rook.core.io.proxy.message.DataType;
import rook.core.io.proxy.message.IOValue;
import rook.core.io.service.IOOutput;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
public class GoPiGoLedLeft implements IOOutput {

	private final Cap cap = new Cap()
			.setCapType(CapType.OUTPUT)
			.setDataType(DataType.BOOLEAN)
			.setMinValue(0)
			.setMaxValue(1)
			.setIncrement(1);
	private final GoPiGoHardware hw;
	
	public GoPiGoLedLeft(RID id, GoPiGoHardware hw) {
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
			hw.writeLeftLed(value.getValueAsBoolean());
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
