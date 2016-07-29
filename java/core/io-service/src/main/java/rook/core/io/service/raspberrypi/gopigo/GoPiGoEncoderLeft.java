package rook.core.io.service.raspberrypi.gopigo;

import java.io.IOException;

import rook.api.RID;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
public class GoPiGoEncoderLeft extends GoPiGoEncoder {

	private final GoPiGoHardware hw;
	
	public GoPiGoEncoderLeft(RID id, GoPiGoHardware hw) {
		super(id);
		this.hw = hw;
	}
	
	@Override
	protected int readValue() throws IOException {
		synchronized (hw) {
			return hw.readLeftEncoder();
		}
	}

}
