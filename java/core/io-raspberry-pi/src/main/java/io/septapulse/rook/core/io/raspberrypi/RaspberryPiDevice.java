package io.septapulse.rook.core.io.raspberrypi;

import io.septapulse.rook.api.exception.InitException;
import io.septapulse.rook.core.io.service.IOService;

/**
 * An underlying device of a {@link RaspberryPiService} {@link IOService}.
 * The Raspberry Pi supports a wide variety of hardware. This interface provides
 * a way to support a multitude of hardware in a generic fasion.
 * 
 * @author Eric Thill
 *
 */
public interface RaspberryPiDevice {
	void init(IOService service) throws InitException;
	void shutdown();
}
