package rook.api.transport;

import java.util.Arrays;
import java.util.Base64;

import org.agrona.ExpandableArrayBuffer;
import org.agrona.MutableDirectBuffer;

/**
 * A buffer backed by a byte[] array that contains a variable length and can
 * grow as necessary to fit a larger payload
 * 
 * @author Eric Thill
 *
 */
public class GrowableBuffer {

	public static GrowableBuffer allocate(int initialCapacity) {
		return new GrowableBuffer(initialCapacity);
	}
	public static GrowableBuffer copyFrom(byte[] src) {
		return new GrowableBuffer(src);
	}
	
	private static final char[] HEX = "0123456789ABCDEF".toCharArray();
	private static final byte ZERO = 0;

	private final int defaultCapacity;
	private MutableDirectBuffer direct;
	private int length;
	
	private GrowableBuffer(byte[] buf) {
		this(buf.length);
		direct.putBytes(0, buf);
		length(buf.length);
	}
	
	private GrowableBuffer(int initialCapacity) {
		this.defaultCapacity = initialCapacity;
		direct = new ExpandableArrayBuffer(initialCapacity);
	}

	/**
	 * Resize the buffer to the initial capacity. This method does not clear the
	 * buffer. If a clean buffer is required, call clean() after calling this
	 * method.
	 * 
	 * @return this
	 */
	public GrowableBuffer reset() {
		if(direct.byteArray().length != defaultCapacity) {
			direct = new ExpandableArrayBuffer(defaultCapacity);
		}
		length = 0;
		return this;
	}

	/**
	 * Fill the current underlying buffer with with 0's
	 * 
	 * @return this
	 */
	public GrowableBuffer clear() {
		Arrays.fill(direct.byteArray(), ZERO);
		return this;
	}

	/**
	 * Return the current length
	 * 
	 * @return length
	 */
	public int length() {
		return length;
	}

	/**
	 * Resize the underlying buffer to fit the given capacity. Optionally, the
	 * existing data in the buffer data can be copied on a resize using
	 * keep=true.
	 * 
	 * @param capacity
	 *            Required capacity
	 * @param keep
	 *            If the existing data in the buffer needs to be kept after a
	 *            resize.
	 * @return this
	 */
	public GrowableBuffer reserve(int capacity, boolean keep) {
		if (capacity > direct.capacity()) {
			if(keep) {
				direct.checkLimit(capacity);
			} else {
				direct = new ExpandableArrayBuffer(capacity);
			}
		}
		return this;
	}

	/**
	 * Set the underlying length. The buffer will be resized as necessary.
	 * 
	 * @param length
	 * @return this
	 */
	public GrowableBuffer length(int length) {
		reserve(length, true);
		this.length = length;
		return this;
	}

	/**
	 * Return the underlying byte array
	 * 
	 * @return byte array
	 */
	public byte[] bytes() {
		return direct.byteArray();
	}

	/**
	 * Return the underlying MutableDirectBuffer backed by the underlying
	 * byte-buffer.
	 * 
	 * @return DirectBuffer
	 */
	public MutableDirectBuffer direct() {
		return direct;
	}
	
	/**
	 * Copy the given buffer's data to this buffer.
	 * @param dest
	 */
	public void copyFrom(GrowableBuffer src) {
		reserve(src.length, false);
		direct.putBytes(0, src.bytes(), 0, src.length);
		length(src.length);
	}

	/**
	 * Copy this buffer's data to the given buffer.
	 * 
	 * @param dest
	 *            The destination buffer
	 */
	public void copyTo(GrowableBuffer dest) {
		dest.reserve(length, false);
		dest.direct.putBytes(0, bytes(), 0, length);
		dest.length(length);
	}

	/**
	 * Copy this buffer's data to a new buffer. The new buffer's capacity
	 * will be equal to the length (extra capacity will be trimmed)
	 * 
	 * @return the new buffer
	 */
	public GrowableBuffer copy() {
		GrowableBuffer copy = new GrowableBuffer(length);
		copyTo(copy);
		return copy;
	}

	@Override
	public int hashCode() {
		int result = 1;
		for(int i = 0; i < length; i++) {
			result = 31 * result + direct.getByte(i);
		}
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GrowableBuffer other = (GrowableBuffer) obj;
		if (length != other.length)
			return false;
		for(int i = 0; i < length; i++)
			if(direct.getByte(i) != other.direct.getByte(i))
				return false;
		return true;
	}
	@Override
	public String toString() {
		return "[ " + toHex() + " ]";
	}
	
	public String toHex() {
		char[] hex = new char[length * 2];
		for (int i = 0; i < length; i++) {
			int val = direct.getByte(i) & 0xFF;
			hex[i * 2] = HEX[val >>> 4];
			hex[i * 2 + 1] = HEX[val & 0x0F];
		}
		return new String(hex);
	}
	
	public static GrowableBuffer fromHex(String hex) {
		final int numBytes = hex.length() / 2;
		GrowableBuffer b = GrowableBuffer.allocate(numBytes);
		final int numChars = hex.length();
		for (int i = 0; i < numChars; i += 2) {
			b.bytes()[i
					/ 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
		}
		b.length(numBytes);
		return b;
	}
	
	public String toBase64() {
		return Base64.getEncoder().encodeToString(
				Arrays.copyOf(bytes(), length()));
	}
	
	public static GrowableBuffer fromBase64(String base64) {
		return new GrowableBuffer(Base64.getDecoder().decode(base64));
	}
	
}
