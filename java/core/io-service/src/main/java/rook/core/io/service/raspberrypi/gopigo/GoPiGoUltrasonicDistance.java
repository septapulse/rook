package rook.core.io.service.raspberrypi.gopigo;

import java.io.IOException;

import rook.api.InitException;
import rook.api.RID;
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
public class GoPiGoUltrasonicDistance implements IOInput {

	private final Cap cap = new Cap()
			.setCapType(CapType.INPUT)
			.setDataType(DataType.INTEGER)
			.setMinValue(0)
			.setMaxValue(200)
			.setIncrement(1);
	private final IOValue value = new IOValue();
	private final RID id;
	private final GoPiGoHardware hw;
	
	public GoPiGoUltrasonicDistance(RID id, GoPiGoHardware hw) {
		this.id = id.unmodifiable();
		this.hw = hw;
		this.cap.setID(id);
	}
	
	@Override
	public void init() throws InitException {

	}

	@Override
	public void shutdown() {

	}

	@Override
	public IOValue read() throws IOException {
		int v = hw.readUltrasonicDistance();
		if(v > 200) {
			v = 200;
		}
		return value.setID(id).setValue(v);
	}

	@Override
	public Cap cap() {
		return cap;
	}

}
