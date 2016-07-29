package rook.api.collections;

/**
 * Functional interface for iteration
 * 
 * @author Eric Thill
 *
 * @param <T>
 */
public interface ElementIterator<T> {
	void accept(T element);
}