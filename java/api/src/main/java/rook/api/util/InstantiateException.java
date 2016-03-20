package rook.api.util;

/**
 * Thrown be {@link Instantiate} methods when instantiation fails.
 * 
 * @author Eric Thill
 *
 */
public class InstantiateException extends Exception {

	private static final long serialVersionUID = 1L;

	public InstantiateException(String message, Throwable cause) {
		super(message, cause);
	}

	public InstantiateException(String message) {
		super(message);
	}

	
}
