package run.rook.api.transport;

/**
 * Serializes a String as a buffer
 * 
 * @author Eric Thill
 *
 */
public class StringSerializer implements Serializer<String> {

	private final GrowableBuffer buf;

	public StringSerializer() {
		this(128);
	}
	
	/**
	 * The default capacity of the underlying {@link GrowableBuffer}
	 * 
	 * @param defaultBufferCapacity
	 */
	public StringSerializer(int defaultBufferCapacity) {
		buf = GrowableBuffer.allocate(defaultBufferCapacity);
	}

	@Override
	public void serialize(String msg, GrowableBuffer dest) {
		dest.length(msg.length());
		for (int i = 0; i < msg.length(); i++) {
			buf.bytes()[i] = (byte) msg.charAt(i);
		}
	}
}
