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
public abstract class GoPiGoEncoder implements IOInput {

	private final Cap cap = new Cap()
			.setCapType(CapType.INPUT)
			.setDataType(DataType.INTEGER)
			.setMinValue(0)
			.setMaxValue(65535)
			.setIncrement(1);
	private final IOValue value = new IOValue();
	private int lastValue = 0;
	private long lastTime = 0;
	
	public GoPiGoEncoder(RID id) {
		this.cap.setID(id.immutable());
	}
	
	@Override
	public void init() throws InitException {

	}

	@Override
	public void shutdown() {

	}

	@Override
	public final IOValue read() throws IOException {
		int newValue = readValue();
		long newTime = System.currentTimeMillis();
		int readsPerSecond;
		if(lastTime == 0) {
			readsPerSecond = 0;
		} else {
			int delta = newValue-lastValue;
			if(delta == 0) {
				readsPerSecond = 0;
			} else {
				if(delta < 0) {
					// account for 16-bit wrap
					delta = 65536-lastValue+newValue;
				}
				double seconds = (newTime-lastTime)/1000.0;
				readsPerSecond = (int)(delta/seconds);
				if(readsPerSecond == 0) {
					// not zero, so don't show '0' speed
					readsPerSecond = 1;
				}
			}
			
		}
		lastValue = newValue;
		lastTime = newTime;
		return value.setValue(readsPerSecond);
	}
	
	protected abstract int readValue() throws IOException;

	@Override
	public RID id() {
		return cap.getId();
	}
	
	@Override
	public final Cap cap() {
		return cap;
	}
	
}
