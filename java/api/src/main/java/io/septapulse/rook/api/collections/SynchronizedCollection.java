package io.septapulse.rook.api.collections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Synchronized class backed by an ArrayList. Conceptually useful as a small
 * thread-safe {@link Set} or a thread-safe {@link List} of any size with
 * minimal garbage creation.
 * 
 * @author Eric Thill
 *
 * @param <T>
 */
public class SynchronizedCollection<T> implements ThreadSafeCollection<T> {

	private final List<T> elements = new ArrayList<>();
	private final EqualsFunction<T> equalsFunc;

	public SynchronizedCollection() {
		this(EqualsFunction.defaultEquals());
	}

	public SynchronizedCollection(EqualsFunction<T> equalsFunc) {
		this.equalsFunc = equalsFunc;
	}

	@Override
	public synchronized void clear() {
		elements.clear();
	}

	@Override
	public synchronized int size() {
		return elements.size();
	}

	@Override
	public synchronized boolean add(T e, boolean allowDup) {
		if (!allowDup && elements.contains(e))
			return false;
		elements.add(e);
		return true;
	}

	@Override
	public synchronized boolean removeFirst(T e) {
		for (int i = 0; i < elements.size(); i++) {
			if (equalsFunc.testEquals(elements.get(i), e)) {
				elements.remove(i);
				return true;
			}
		}
		return false;
	}

	@Override
	public synchronized boolean removeAll(T e) {
		boolean removed = false;
		for (int i = 0; i < elements.size(); i++) {
			if (equalsFunc.testEquals(elements.get(i), e)) {
				elements.remove(i);
				removed = true;
				i--;
			}
		}
		return removed;
	}

	@Override
	public synchronized boolean removeIf(Predicate<T> filter) {
		return elements.removeIf(filter);
	}

	@Override
	public synchronized boolean contains(T e) {
		return elements.contains(e);
	}

	@Override
	public synchronized T pollHead() {
		if (elements.size() == 0)
			return null;
		return elements.remove(0);
	}

	@Override
	public synchronized T popTail() {
		if (elements.size() == 0)
			return null;
		return elements.remove(elements.size() - 1);
	}

	@Override
	public synchronized void iterate(ElementIterator<T> iter) {
		for (int i = 0; i < elements.size(); i++) {
			iter.accept(elements.get(i));
		}
	}

	@Override
	public synchronized void iterate(ElementIndexIterator<T> iter) {
		for (int i = 0; i < elements.size(); i++) {
			iter.accept(elements.get(i), i, elements.size());
		}
	}

}
