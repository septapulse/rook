package run.rook.core.io.proxy;

/**
 * Functional interface to listen to IO Batch complete
 * 
 * @author Eric Thill
 *
 */
public interface IOCompleteListener {
	void onComplete();
}
