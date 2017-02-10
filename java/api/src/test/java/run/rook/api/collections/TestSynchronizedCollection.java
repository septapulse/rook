package run.rook.api.collections;

import run.rook.api.collections.SynchronizedCollection;
import run.rook.api.collections.ThreadSafeCollection;

public class TestSynchronizedCollection extends TestThreadSafeCollection {

	@Override
	protected ThreadSafeCollection<String> createCollection() {
		return new SynchronizedCollection<>();
	}
	
}