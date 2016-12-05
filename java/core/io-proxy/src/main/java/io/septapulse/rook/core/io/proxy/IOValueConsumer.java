package io.septapulse.rook.core.io.proxy;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.core.io.proxy.message.IOValue;

/**
 * Functional interface to consume {@link IOValue}s
 * 
 * @author Eric Thill
 *
 */
public interface IOValueConsumer {
	/**
	 * @param id
	 *            The input or outputs ID
	 * @param value
	 *            The value of the input or output
	 */
	void onValue(RID id, IOValue value);
}
