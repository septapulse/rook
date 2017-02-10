package run.rook.core.io.service.dummy;

import java.io.IOException;

import run.rook.api.RID;
import run.rook.api.exception.InitException;
import run.rook.core.io.proxy.message.Cap;
import run.rook.core.io.proxy.message.CapType;
import run.rook.core.io.proxy.message.DataType;
import run.rook.core.io.proxy.message.IOValue;
import run.rook.core.io.service.IOOutput;

/**
 * Used for testing {@link IOOutput} logic
 * 
 * @author Eric Thill
 *
 */
public class DummyIOOutput implements IOOutput {

	private final Cap cap;
	private final RID id;
	
	public DummyIOOutput(RID id, DataType dataType) {
		cap = new Cap()
				.setCapType(CapType.OUTPUT)
				.setDataType(dataType)
				.setID(id.copy());
		if(dataType == DataType.BOOLEAN) {
			cap.setIncrement(1)
					.setMinValue(0)
					.setMaxValue(1);
		} else if(dataType == DataType.FLOAT) {
			cap.setIncrement(0.5)
					.setMinValue(0)
					.setMaxValue(255);
		} else if(dataType == DataType.INTEGER) {
			cap.setIncrement(1)
					.setMinValue(0)
					.setMaxValue(255);
		}
		this.id = id.immutable();
	}

	@Override
	public void init() throws InitException {
		
	}

	@Override
	public void shutdown() {
		
	}

	@Override
	public void write(IOValue value) throws IOException {
		
	}

	@Override
	public RID id() {
		return id;
	}
	
	@Override
	public Cap cap() {
		return cap;
	}
}
