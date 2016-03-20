package rook.api.lang.operations;

import rook.api.lang.Interrupt;
import rook.api.lang.Operation;
import rook.api.lang.Scope;
import rook.api.lang.Variable;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
public class BangOperation extends Operation {

	private final Operation op;
	private final Variable result = new Variable();
	
	public BangOperation(Operation op) {
		this.op = op;
	}
	
	@Override
	public Variable exe(Scope globalScope, Scope operationScope, Interrupt interrupt) {
		return result.set(!op.execute(globalScope, operationScope, interrupt).booleanValue());
	}
 
	@Override
	public String toString() {
		return "!" + op;
	}
	
	@Override
	public String toString(int tab) {
		return "!" + op.toString(tab);
	}
}
