package run.rook.examples.io.gopigo;

/**
 * Functional Interface that represents a behavior for a Bottom-Up action
 * 
 * @author Eric Thill
 *
 */
public interface BottomUpBehavior {
	/**
	 * Execute the behavior
	 * 
	 * @return true if conditions were met for this behavior, false otherwise
	 */
	boolean execute();
}
