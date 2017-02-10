package run.rook.core.io.proxy.message;

/**
 * A {@link IOValue}'s data type
 * 
 * @author Eric Thill
 *
 */
public enum DataType {
	BOOLEAN((byte)0), INTEGER((byte)1), FLOAT((byte)2), BUFFER((byte)3),
	STRING((byte)4), IMAGE_BITMAP((byte)5), IMAGE_PNG((byte)6);
	
	private final byte value;
	
	private DataType(byte value) {
		this.value = value;
	}
	
	public byte getValue() {
		return value;
	}
	
	public static DataType fromValue(byte value) {
		for(DataType ct : DataType.values()) {
			if(ct.value == value) {
				return ct;
			}
		}
		throw new IllegalArgumentException(""+value);
	}
}
