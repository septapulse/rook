package run.rook.api.collections;

import java.util.function.Predicate;

/**
 * Thread safe collection with built-in List<>, Set<>, and Dequeue<>
 * functionality.
 * 
 * @author Eric Thill
 *
 * @param <T>
 */
public interface ThreadSafeCollection<T> {
	/**
	 * Clear the underlying data
	 */
	void clear();

	/**
	 * Get the number of elements
	 * 
	 * @return Number of elements
	 */
	int size();

	/**
	 * Add an element to the collection
	 * 
	 * @param e
	 *            The element to add
	 * @param allowDup
	 *            If a duplicate of this element is allowed. False -> Set
	 *            functionality, True -> List functionality.
	 * @return True if added, False otherwise. False can only be returned when
	 *         allowDup=false.
	 */
	boolean add(T e, boolean allowDup);

	/**
	 * Remove the first instance of the given element. Determining if an element
	 * is equal to this one is up to the implementing class, but typically an
	 * {@link EqualsFunction} will be used.
	 * 
	 * @param e
	 *            The element to remove
	 * @return true if the element existed (and thus removed), false otherwise
	 */
	boolean removeFirst(T e);

	/**
	 * Remove all instances of the given element. Determining if an element is
	 * equal to this one is up to the implementing class, but typically an
	 * {@link EqualsFunction} will be used.
	 * 
	 * @param e
	 *            The element to remove
	 * @return true if the element existed (and thus removed), false otherwise
	 */
	boolean removeAll(T e);

	/**
	 * Remove all instances as determined by the given predicate
	 * 
	 * @param pred
	 *            The predicate to use to check each element
	 * @return true if any elements were removed, false otherwise
	 */
	boolean removeIf(Predicate<T> pred);

	/**
	 * Check if the given element exists in the underlying data. Determining if
	 * an element is equal to this one is up to the implementing class, but
	 * typically an {@link EqualsFunction} will be used.
	 * 
	 * @param e
	 *            The element to check
	 * @return true if the element exists in the underlying data, false
	 *         otherwise.
	 */
	boolean contains(T e);

	/**
	 * Remove and return the first element in the collection. Queue<>
	 * functionality.
	 * 
	 * @return
	 */
	T pollHead();

	/**
	 * Remove and return the last element in the collection. Stack<>/Dequeue<>
	 * functionality.
	 * 
	 * @return
	 */
	T popTail();

	/**
	 * Iterate through all of the elements in the underlying collection by
	 * visiting them with the given functional interface.
	 * 
	 * @param iter
	 *            The functional interface all references in the underlying
	 *            collection will be passed to.
	 */
	void iterate(ElementIterator<T> iter);

	/**
	 * Iterate through all of the elements in the underlying collection by
	 * visiting them and their associated index with the given functional
	 * interface.
	 * 
	 * @param iter
	 *            The functional interface all references in the underlying
	 *            collection will be passed to.
	 */
	void iterate(ElementIndexIterator<T> iter);
}
