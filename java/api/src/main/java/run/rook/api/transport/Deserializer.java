package run.rook.api.transport;

/**
 * Interface for a class that can deserialize a buffer into a generic type
 * 
 * @author Eric Thill
 *
 * @param <T>
 *            The type that will be returned when a message is deserialized
 */
public interface Deserializer<T> {

	/**
	 * Deserialize a message into the generic class
	 * 
	 * @param msg
	 *            The serialized message
	 * @return The deserialized message, or null if the message could not be
	 *         deserialized
	 */
	T deserialize(GrowableBuffer msg);

}
