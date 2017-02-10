package run.rook.core.io.raspberrypi.gopigo;

import java.io.IOException;

import run.rook.api.RID;
import run.rook.api.exception.InitException;
import run.rook.core.io.proxy.message.Cap;
import run.rook.core.io.proxy.message.CapType;
import run.rook.core.io.proxy.message.DataType;
import run.rook.core.io.proxy.message.IOValue;
import run.rook.core.io.service.IOOutput;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
public class GoPiGoMotorRight implements IOOutput {

	private final Cap cap = new Cap()
			.setCapType(CapType.OUTPUT)
			.setDataType(DataType.INTEGER)
			.setMinValue(-255)
			.setMaxValue(255)
			.setIncrement(1);
	private final GoPiGoHardware hw;
	
	public GoPiGoMotorRight(RID id, GoPiGoHardware hw) {
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
			hw.writeRightMotor(value.getValueAsInt());
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
