package rook.api.lang;

/**
 * Interrupt the execution of an application
 * 
 * @author Eric Thill
 *
 */
public class Interrupt {
	private volatile boolean interrupted = false;
	public boolean isInterrupted() {
		if(interrupted) {
			interrupted = false;
			return true;
		} else {
			return false;
		}
	}
	public void interrupt() {
		interrupted = true;
	}
}
