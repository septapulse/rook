package rook.api.util;

/**
 * Utility methods to operate on a byte array
 * 
 * @author Eric Thill
 *
 */
public class BufferUtil {

	public static final byte[] EMPTY_BUFFER = new byte[0];

	public static void writeLong(byte[] dst, int dstOff, long v) {
		dst[dstOff] = (byte)(v & 0xFF);
		dst[dstOff+1] = (byte)((v >> 8) & 0xFF);
		dst[dstOff+2] = (byte)((v >> 16) & 0xFF);
		dst[dstOff+3] = (byte)((v >> 24) & 0xFF);
		dst[dstOff+4] = (byte)((v >> 32) & 0xFF);
		dst[dstOff+5] = (byte)((v >> 40) & 0xFF);
		dst[dstOff+6] = (byte)((v >> 48) & 0xFF);
		dst[dstOff+7] = (byte)((v >> 56) & 0xFF);
	}
	
	public static void writeBuf(byte[] dst, int dstOff, byte[] src, int srcOff, int len) {
		System.arraycopy(src, srcOff, dst, dstOff, len);
	}
	
	public static long readLong(byte[] src, int srcOff) {
		long v = 0;
		v |= ((long)(src[srcOff] & 0xFF));
		v |= (((long)(src[srcOff+1] & 0xFF)) << 8);
		v |= (((long)(src[srcOff+2] & 0xFF)) << 16);
		v |= (((long)(src[srcOff+3] & 0xFF)) << 24);
		v |= (((long)(src[srcOff+4] & 0xFF)) << 32);
		v |= (((long)(src[srcOff+5] & 0xFF)) << 40);
		v |= (((long)(src[srcOff+6] & 0xFF)) << 48);
		v |= ((long)((src[srcOff+7] & 0xFF)) << 56);
		return v;
	}
}
