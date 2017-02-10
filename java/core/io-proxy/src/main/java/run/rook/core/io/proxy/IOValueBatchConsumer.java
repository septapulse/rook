package run.rook.core.io.proxy;

import run.rook.api.RID;
import run.rook.core.io.proxy.message.IOValue;

/**
 * Functional interface to consume {@link IOValue}s with an indicator about
 * which value is the last value in the batch
 * 
 * @author Eric Thill
 *
 */
public interface IOValueBatchConsumer {
	/**
	 * @param id
	 *            The input or outputs ID
	 * @param value
	 *            The value of the input or output
	 * @param endOfBatch
	 *            True if this is the last value in a batch. False indicates
	 *            another value will immediately follow this one.
	 */
	void onValue(RID id, IOValue value, boolean endOfBatch);
}
