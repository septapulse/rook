package io.septapulse.rook.api.collections;

import io.septapulse.rook.api.collections.SynchronizedCollection;
import io.septapulse.rook.api.collections.ThreadSafeCollection;

public class TestSynchronizedCollection extends TestThreadSafeCollection {

	@Override
	protected ThreadSafeCollection<String> createCollection() {
		return new SynchronizedCollection<>();
	}
	
}