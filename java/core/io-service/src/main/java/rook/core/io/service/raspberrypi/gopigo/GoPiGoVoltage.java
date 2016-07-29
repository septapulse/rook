package rook.core.io.service.raspberrypi.gopigo;

import java.io.IOException;

import rook.api.RID;
import rook.api.exception.InitException;
import rook.core.io.proxy.message.Cap;
import rook.core.io.proxy.message.CapType;
import rook.core.io.proxy.message.DataType;
import rook.core.io.proxy.message.IOValue;
import rook.core.io.service.IOInput;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
public class GoPiGoVoltage implements IOInput {

	private final Cap cap = new Cap()
			.setCapType(CapType.INPUT)
			.setDataType(DataType.FLOAT)
			.setMinValue(0)
			.setMaxValue(32)
			.setIncrement(Double.MIN_VALUE);
	private final IOValue value = new IOValue();
	private final GoPiGoHardware hw;
	
	public GoPiGoVoltage(RID id, GoPiGoHardware hw) {
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
	public IOValue read() throws IOException {
		double v;
		synchronized (hw) {
			v = hw.readVoltage();
		}
		return value.setValue(v);
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
