package io.septapulse.rook.daemon.websocket;

public class ProcessManagerException extends Exception {

	private static final long serialVersionUID = 1L;

	public ProcessManagerException() {
		super();
	}

	public ProcessManagerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessManagerException(String message) {
		super(message);
	}

	public ProcessManagerException(Throwable cause) {
		super(cause);
	}

}
