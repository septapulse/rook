package io.septapulse.rook.api.collections;

import io.septapulse.rook.api.collections.AtomicCollection;
import io.septapulse.rook.api.collections.ThreadSafeCollection;

public class TestAtomicCollection extends TestThreadSafeCollection {

	@Override
	protected ThreadSafeCollection<String> createCollection() {
		return new AtomicCollection<>();
	}
	
}