package rook.api;

/**
 * Initialization Exception
 * 
 * @author Eric Thill
 *
 */
public class InitException extends Exception {

	private static final long serialVersionUID = 1L;

	public InitException() {
		super();
	}

	public InitException(String message, Throwable cause) {
		super(message, cause);
	}

	public InitException(String message) {
		super(message);
	}

	public InitException(Throwable cause) {
		super(cause);
	}
	
}
