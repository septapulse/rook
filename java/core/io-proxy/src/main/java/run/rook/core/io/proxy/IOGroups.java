package run.rook.core.io.proxy;

import run.rook.api.RID;

/**
 * Constants
 * 
 * @author Eric Thill
 *
 */
public class IOGroups {

	private IOGroups() {
		
	}
	
	public static final RID INPUT = RID.create("IO.INPUT").immutable();
	public static final RID OUTPUT = RID.create("IO.OUTPUT").immutable();
	public static final RID PROBE = RID.create("IO.PROBE").immutable();
	public static final RID CAPS = RID.create("IO.CAPS").immutable();
	
}
