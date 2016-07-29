package rook.core.io.service.raspberrypi;

import rook.api.exception.InitException;
import rook.core.io.service.IOManager;
import rook.core.io.service.IOService;

/**
 * An underlying device of a {@link RaspberryPi} {@link IOService}.
 * The Raspberry Pi supports a wide variety of hardware. This interface provides
 * a way to support a multitude of hardware in a generic fasion.
 * 
 * @author Eric Thill
 *
 */
public interface RaspberryPiDevice {
	void init(IOManager ioManager) throws InitException;
	void shutdown();
}
