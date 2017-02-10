package run.rook.api.collections;

import run.rook.api.collections.AtomicCollection;
import run.rook.api.collections.ThreadSafeCollection;

public class TestAtomicCollection extends TestThreadSafeCollection {

	@Override
	protected ThreadSafeCollection<String> createCollection() {
		return new AtomicCollection<>();
	}
	
}