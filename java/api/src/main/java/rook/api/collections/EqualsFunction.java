package rook.api.collections;

/**
 * Functional interface for determining if two elements are equal
 * 
 * @author Eric Thill
 *
 * @param <T>
 */
public interface EqualsFunction<T> {
	boolean testEquals(T o1, T o2);

	/**
	 * Takes null into account (null==null) and uses obj.equals(obj)
	 * 
	 * @return Default EqualsFunction
	 */
	public static <T> EqualsFunction<T> defaultEquals() {
		return (T o1, T o2) -> {
			if (o1 == null && o2 == null) {
				return true;
			} else if (o1 == null || o2 == null) {
				return false;
			} else if (o1 == o2) {
				return true;
			} else {
				return o1.equals(o2);
			}
		};
	}
}
