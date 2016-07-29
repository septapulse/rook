package rook.core.io.proxy;

import rook.api.RID;

/**
 * Constants
 * 
 * @author Eric Thill
 *
 */
public class IOGroups {

	private IOGroups() {
		
	}
	
	public static RID INPUT = RID.create("IO.INPUT").immutable();
	public static RID OUTPUT = RID.create("IO.OUTPUT").immutable();
	public static RID PROBE = RID.create("IO.PROBE").immutable();
	public static RID CAPS = RID.create("IO.CAPS").immutable();
	
}
