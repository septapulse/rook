package rook.api.collections;

/**
 * Functional interface for iteration
 * 
 * @author Eric Thill
 *
 * @param <T>
 */
public interface ElementIndexIterator<T> {
	void accept(T element, int idx, int length);
}