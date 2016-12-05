package io.septapulse.rook.core.io.service.raspberrypi.gopigo;

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
public class GoPiGoUltrasonicDistance implements IOInput {

	private final Cap cap = new Cap()
			.setCapType(CapType.INPUT)
			.setDataType(DataType.INTEGER)
			.setMinValue(0)
			.setMaxValue(200)
			.setIncrement(1);
	private final IOValue value = new IOValue();
	private final GoPiGoHardware hw;
	
	public GoPiGoUltrasonicDistance(RID id, GoPiGoHardware hw) {
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
		int v;
		synchronized (hw) {
			v = hw.readUltrasonicDistance();
		}
		if(v > 200) {
			v = 200;
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
