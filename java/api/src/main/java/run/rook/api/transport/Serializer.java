package run.rook.api.transport;

/**
 * Interface for a class that can serialize a generic type into a buffer
 * 
 * @author Eric Thill
 *
 * @param <T>
 *            The type that will be serialized into a buffer
 */
public interface Serializer<T> {
	void serialize(T msg, GrowableBuffer dest);
}
