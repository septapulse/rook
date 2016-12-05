package io.septapulse.rook.core.io.service.dummy;

import java.io.IOException;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.exception.InitException;
import io.septapulse.rook.core.io.proxy.message.Cap;
import io.septapulse.rook.core.io.proxy.message.CapType;
import io.septapulse.rook.core.io.proxy.message.DataType;
import io.septapulse.rook.core.io.proxy.message.IOValue;
import io.septapulse.rook.core.io.service.IOInput;

/**
 * Used for testing {@link IOInput} logic 
 * 
 * @author Eric Thill
 *
 */
public class DummyIOInput implements IOInput {

	private final IOValue value = new IOValue();
	private final RID id;
	private final Cap cap;
	private final DataType dataType;
	private double val = 1;
	private double increment = 1;
	
	public DummyIOInput(RID id, DataType dataType) {
		this.id = id.immutable();
		this.dataType = dataType;
		this.cap = new Cap()
				.setCapType(CapType.INPUT)
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
			increment = 0.5;
		} else if(dataType == DataType.INTEGER) {
			cap.setIncrement(1)
					.setMinValue(0)
					.setMaxValue(255);
		}
	}
	
	@Override
	public void init() throws InitException {

	}

	@Override
	public void shutdown() {

	}

	@Override
	public IOValue read() throws IOException {
		if(dataType == DataType.BOOLEAN) {
			if(val == 0) {
				val = 1;
			} else {
				val = 0;
			}
			value.setValue(val != 0);
		} else if(dataType == DataType.INTEGER || dataType == DataType.FLOAT){
			val+=increment;
			if(val > 255) {
				val = 255;
				increment *= -1;
			} else if(val < 0) {
				val = 0;
				increment *= -1;
			}
			if(dataType == DataType.INTEGER) {
				value.setValue((int)val);
			} else {
				value.setValue(val);
			}
		}
		
		return value;
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
