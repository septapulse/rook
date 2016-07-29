package rook.api.util;

/**
 * Utility for sleeping
 * 
 * @author Eric Thill
 *
 */
public class Sleep {

	/**
	 * Try to sleep without worrying about an exception being throw. An
	 * interrupt will return immediately instead of throw an error. This is
	 * appropriate behavior when the sleep is followed immediately by a check on
	 * a volatile/atomic variable to determine if the thread should exit.
	 * 
	 * @param millis
	 *            Number of milliseconds to sleep
	 */
	public static void trySleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// return now
		}
	}
}
