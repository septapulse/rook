package rook.api.transport;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * A buffer backed by a byte[] array that contains a variable length and can
 * grow as necessary to fit a larger payload
 * 
 * @author Eric Thill
 *
 */
public class GrowableBuffer {

	public static GrowableBuffer allocate(int defaultCapacity) {
		return new GrowableBuffer(new byte[defaultCapacity]);
	}

	private final int defaultCapacity;
	private byte[] bytes;
	private int length;

	private GrowableBuffer(byte[] bytes) {
		this.bytes = bytes;
		defaultCapacity = bytes.length;
	}

	/**
	 * Returns the buffer to it's original capacity, and optionally will fill it
	 * with 0's
	 * 
	 * @param fill
	 *            Specifies if the underling buffer should be filled with 0's
	 * @return this (for stringing together commands)
	 */
	public GrowableBuffer reset(boolean fill) {
		if (bytes.length != defaultCapacity) {
			bytes = new byte[defaultCapacity];
		} else if (fill) {
			Arrays.fill(bytes, (byte) 0);
		}
		length = 0;
		return this;
	}

	/**
	 * Grows the underlying buffer to the given length
	 * 
	 * @param length
	 *            The required size of the payload that this buffer will need to
	 *            contain
	 * @param copy
	 *            If true, existing contents will be copied if the underlying
	 *            buffer is grown
	 * @return this (for stringing together commands)
	 */
	public GrowableBuffer reserve(int length, boolean copy) {
		if (length > bytes.length) {
			if(copy) {
				byte[] existing = bytes;
				bytes = new byte[length];
				System.arraycopy(existing, 0, bytes, 0, existing.length);
			} else {
				bytes = new byte[length];
			}
		}
		return this;
	}

	/**
	 * Get the underling byte buffer
	 * 
	 * @return the underlying byte buffer
	 */
	public byte[] getBytes() {
		return bytes;
	}

	/**
	 * Get the current payload length
	 * 
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Set the underling payload length
	 * 
	 * @param length
	 * @return this (for stringing together commands)
	 */
	public GrowableBuffer setLength(int length) {
		this.length = length;
		return this;
	}

	/**
	 * Add the given buffer to the end of the current payload
	 * 
	 * @param src
	 *            the buffer to be added
	 * @return this (for stringing together commands)
	 */
	public GrowableBuffer put(ByteBuffer src) {
		int srcLen = src.remaining();
		reserve(length + srcLen, true);
		src.get(bytes, length, srcLen);
		length += srcLen;
		return this;
	}

	/**
	 * Add the given buffer to the end of the current payload
	 * 
	 * @param src
	 *            the buffer to be added
	 * @return this (for stringing together commands)
	 */
	public GrowableBuffer put(GrowableBuffer src) {
		put(src.bytes, 0, src.length);
		return this;
	}

	/**
	 * Add the given buffer to the end of the current payload
	 * 
	 * @param src
	 *            the buffer to be added
	 * @return this (for stringing together commands)
	 */
	public GrowableBuffer put(byte[] src) {
		return put(src, 0, src.length);
	}

	/**
	 * Add the given buffer to the end of the current payload
	 * 
	 * @param src
	 *            the buffer to be added
	 * @param off
	 *            the src buffer offset
	 * @param len
	 *            the src buffer length
	 * @return this (for stringing together commands)
	 */
	public GrowableBuffer put(byte[] src, int off, int len) {
		reserve(length + len, true);
		System.arraycopy(src, off, bytes, length, len);
		length += len;
		return this;
	}

	/**
	 * Overwrite the content of this buffer with the content of the given buffer
	 * 
	 * @param src
	 *            the buffer to copy from
	 * @return this (for stringing together commands)
	 */
	public GrowableBuffer copyFrom(GrowableBuffer src) {
		length = 0;
		reserve(src.length, false);
		put(src);
		return this;
	}

	@Override
	public String toString() {
		return "0x" + toHex(bytes, 0, length);
	}
	
	private static String toHex(byte[] b, int off, int len) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < len; i++) {
			String hex = Integer.toHexString(b[off + i] & 0xFF).toUpperCase();
			if (hex.length() == 1) {
				sb.append("0");
			}
			sb.append(hex);
		}
		return sb.toString();
	}

}
