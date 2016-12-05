package io.septapulse.rook.core.io.service.raspberrypi.gopigo;

import java.io.IOException;

import io.septapulse.rook.api.RID;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
public class GoPiGoEncoderRight extends GoPiGoEncoder {

	private final GoPiGoHardware hw;
	
	public GoPiGoEncoderRight(RID id, GoPiGoHardware hw) {
		super(id);
		this.hw = hw;
	}
	
	@Override
	protected int readValue() throws IOException {
		synchronized (hw) {
			return hw.readRightEncoder();
		}
	}

}
