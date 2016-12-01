package rook.ui.environment;

import java.io.IOException;

/**
 * Used by the {@link RuntimeManager} for dispatching log information
 * 
 * @author Eric Thill
 *
 */
@Deprecated
public interface LogSender {
	/**
	 * Dispatch another line of the log
	 * 
	 * @param line
	 * @throws IOException
	 */
	void send(String line) throws IOException;

	/**
	 * Checks if the stream is still value
	 * 
	 * @return
	 */
	boolean isOpen();
}
