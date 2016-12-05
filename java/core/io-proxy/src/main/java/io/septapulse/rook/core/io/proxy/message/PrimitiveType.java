package io.septapulse.rook.core.io.proxy.message;

/**
 * The underlying primitive type of an {@link IOValue}
 * 
 * @author Eric Thill
 *
 */
public enum PrimitiveType {
	OPAQUE((byte)0),
	TWOS_COMPLIMENT_INT((byte)1), 
	IEEE_754_FLOAT((byte)2),
	BOOLEAN((byte)3),
	UTF_8((byte)4);
	
	private final byte value;
	
	private PrimitiveType(byte value) {
		this.value = value;
	}
	
	public byte getValue() {
		return value;
	}
	
	public static PrimitiveType fromValue(byte value) {
		for(PrimitiveType ct : PrimitiveType.values()) {
			if(ct.value == value) {
				return ct;
			}
		}
		throw new IllegalArgumentException(""+value);
	}
}
