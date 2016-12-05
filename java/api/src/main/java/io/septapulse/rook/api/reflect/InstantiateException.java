package io.septapulse.rook.api.reflect;

/**
 * Used by {@link Instantiate} to communicate issues while instantiating in a
 * single {@link Exception}
 * 
 * @author Eric Thill
 *
 */
public class InstantiateException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InstantiateException() {
		super();
	}

	public InstantiateException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public InstantiateException(String arg0) {
		super(arg0);
	}

	public InstantiateException(Throwable arg0) {
		super(arg0);
	}

}
