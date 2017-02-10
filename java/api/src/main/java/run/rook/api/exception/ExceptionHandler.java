package run.rook.api.exception;

/**
 * Asynchronous exception handler. Used by a multi-threaded utility to report
 * errors back to developer-land.
 * 
 * @author Eric Thill
 *
 */
public interface ExceptionHandler {
	void error(String message, Throwable t);
}
