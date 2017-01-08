package io.septapulse.rook.core.io.proxy;

/**
 * Constants
 * 
 * @author Eric Thill
 *
 */
public class IOMessageConst {

	private IOMessageConst() {
		
	}

	public static final byte GET_CAPABILITIES = 0;
	public static final byte GET_INPUT = 1;
	public static final byte GET_OUTPUT = 2;
	public static final byte SET_OUTPUT = 3;
	
	public static final byte SUCCESS = 0;
	public static final byte FAILED = 1;
	
}
