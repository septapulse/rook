package run.rook.core.io.proxy;

/**
 * Functional interface to trigger an update. This is useful to trigger logic as
 * soon as new values arrive.
 * 
 * @author Eric Thill
 *
 */
public interface IOUpdateListener {
	void onUpdate();
}
