package io.septapulse.rook.core.io.proxy.message;

/**
 * INPUT or OUTPUT
 * 
 * @author Eric Thill
 *
 */
public enum CapType {
	INPUT((byte)0), OUTPUT((byte)1);
	
	private final byte value;
	
	private CapType(byte value) {
		this.value = value;
	}
	
	public byte getValue() {
		return value;
	}
	
	public static CapType fromValue(byte value) {
		for(CapType ct : CapType.values()) {
			if(ct.value == value) {
				return ct;
			}
		}
		throw new IllegalArgumentException(""+value);
	}
}
