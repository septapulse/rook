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
public class GoPiGoMotorLeft implements IOOutput {

	private final Cap cap = new Cap()
			.setCapType(CapType.OUTPUT)
			.setDataType(DataType.INTEGER)
			.setMinValue(-255)
			.setMaxValue(255)
			.setIncrement(1);
	private final GoPiGoHardware hw;
	
	public GoPiGoMotorLeft(RID id, GoPiGoHardware hw) {
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
			hw.writeLeftMotor(value.getValueAsInt());
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
