package rook.api.collections;

public class TestAtomicCollection extends TestThreadSafeCollection {

	@Override
	protected ThreadSafeCollection<String> createCollection() {
		return new AtomicCollection<>();
	}
	
}