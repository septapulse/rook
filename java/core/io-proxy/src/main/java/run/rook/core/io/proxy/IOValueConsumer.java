package run.rook.core.io.proxy;

import run.rook.api.RID;
import run.rook.core.io.proxy.message.IOValue;

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
