package rook.api.collections;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

/**
 * Uses atomic reference swaps to operate on an underlying array in a
 * thread-safe manner. This implementation creates garbage every modify
 * operation, so it is best used in settings where normal operation consists
 * mainly of reads.
 * 
 * @author Eric Thill
 *
 * @param <T>
 */
public class AtomicCollection<T> implements ThreadSafeCollection<T> {
	private final EqualsFunction<T> equalsFunc;
	private final AtomicReference<T[]> elements;
	private boolean yield = true;

	public AtomicCollection() {
		this(EqualsFunction.defaultEquals());
	}

	public AtomicCollection(EqualsFunction<T> equalsFunc) {
		this.equalsFunc = equalsFunc;
		this.elements = new AtomicReference<>(allocate(0));
	}

	public AtomicCollection<T> withYield(boolean yield) {
		this.yield = yield;
		return this;
	}

	private T[] getForRead() {
		T[] ref;
		while ((ref = elements.get()) == null) {
			if (yield) {
				Thread.yield();
			}
		}
		return ref;
	}

	private T[] checkoutForWrite() {
		T[] ref;
		while ((ref = elements.getAndSet(null)) == null) {
			if (yield) {
				Thread.yield();
			}
		}
		return ref;
	}

	private void checkinFromWrite(T[] ref) {
		elements.set(ref);
	}

	@Override
	public void clear() {
		elements.set(allocate(0));
	}

	@Override
	public int size() {
		return getForRead().length;
	}

	@Override
	public boolean add(T e, boolean allowDup) {
		if (e == null) {
			throw new NullPointerException("Cannot add null element");
		}
		T[] prevRef = checkoutForWrite();
		if (!allowDup) {
			for (int i = 0; i < prevRef.length; i++) {
				if (equalsFunc.testEquals(prevRef[i], e)) {
					// duplicates not allowed and already exists
					checkinFromWrite(prevRef);
					return false;
				}
			}
		}
		T[] newRef = allocate(prevRef.length + 1);
		System.arraycopy(prevRef, 0, newRef, 0, prevRef.length);
		newRef[prevRef.length] = e;
		checkinFromWrite(newRef);
		return true;
	}

	@Override
	public boolean removeFirst(T e) {
		if (e == null) {
			return false;
		}
		T[] prevRef = checkoutForWrite();
		int removeIdx = -1;
		for (int i = 0; i < prevRef.length; i++) {
			if (equalsFunc.testEquals(prevRef[i], e)) {
				removeIdx = i;
				break;
			}
		}
		if (removeIdx == -1) {
			// nothing removed. checkin previous reference.
			checkinFromWrite(prevRef);
			return false;
		} else {
			T[] newRef = allocate(prevRef.length - 1);
			int prevIdx = 0;
			int newIdx = 0;
			while (newIdx < newRef.length) {
				while (prevIdx == removeIdx)
					prevIdx++;
				newRef[newIdx++] = prevRef[prevIdx++];
			}
			checkinFromWrite(newRef);
			return true;
		}
	}

	@Override
	public boolean removeAll(final T e) {
		if (e == null)
			return false;
		return removeIf(existing -> equalsFunc.testEquals(existing, e));
	}

	@Override
	public boolean removeIf(Predicate<T> pred) {
		T[] prevRef = checkoutForWrite();
		int numRemoved = 0;
		for (int i = 0; i < prevRef.length; i++) {
			if (pred.test(prevRef[i])) {
				prevRef[i] = null;
				numRemoved++;
			}
		}
		if (numRemoved == 0) {
			// nothing removed. checkin previous reference.
			checkinFromWrite(prevRef);
			return false;
		} else {
			T[] newRef = allocate(prevRef.length - numRemoved);
			int prevIdx = 0;
			int newIdx = 0;
			while (newIdx < newRef.length) {
				while (prevRef[prevIdx] == null)
					prevIdx++;
				newRef[newIdx++] = prevRef[prevIdx++];
			}
			checkinFromWrite(newRef);
			return true;
		}
	}

	@Override
	public boolean contains(T e) {
		T[] elements = getForRead();
		for (int i = 0; i < elements.length; i++) {
			if (equalsFunc.testEquals(elements[i], e)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public T pollHead() {
		T[] prevRef = checkoutForWrite();
		if (prevRef.length == 0) {
			checkinFromWrite(prevRef);
			return null;
		}
		T[] newRef = allocate(prevRef.length - 1);
		System.arraycopy(prevRef, 1, newRef, 0, newRef.length);
		checkinFromWrite(newRef);
		return prevRef[0];
	}

	@Override
	public T popTail() {
		T[] prevRef = checkoutForWrite();
		if (prevRef.length == 0) {
			checkinFromWrite(prevRef);
			return null;
		}
		T[] newRef = allocate(prevRef.length - 1);
		System.arraycopy(prevRef, 0, newRef, 0, newRef.length);
		checkinFromWrite(newRef);
		return prevRef[prevRef.length - 1];
	}

	@Override
	public void iterate(ElementIterator<T> t) {
		T[] ref = getForRead();
		for (int i = 0; i < ref.length; i++) {
			t.accept(ref[i]);
		}
	}

	@Override
	public void iterate(ElementIndexIterator<T> t) {
		T[] ref = getForRead();
		for (int i = 0; i < ref.length; i++) {
			t.accept(ref[i], i, ref.length);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] allocate(int size) {
		return (T[]) new Object[size];
	}

}
