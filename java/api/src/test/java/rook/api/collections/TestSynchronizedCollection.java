package rook.api.collections;

public class TestSynchronizedCollection extends TestThreadSafeCollection {

	@Override
	protected ThreadSafeCollection<String> createCollection() {
		return new SynchronizedCollection<>();
	}
	
}