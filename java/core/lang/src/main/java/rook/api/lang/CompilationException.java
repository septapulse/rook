package rook.api.lang;

/**
 * Compilation Exception
 * 
 * @author Eric Thill
 *
 */
public class CompilationException extends Exception {

	private static final long serialVersionUID = 1L;

	public CompilationException(String message, Throwable cause) {
		super(message, cause);
	}

	public CompilationException(String message) {
		super(message);
	}

	public CompilationException(Throwable cause) {
		super(cause);
	}
	
}
