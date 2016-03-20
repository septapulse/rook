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
public class DivideOperation extends Operation {

	private final Operation o1;
	private final Operation o2;
	private final Variable result = new Variable();
	
	public DivideOperation(Operation o1, Operation o2) {
		this.o1 = o1;
		this.o2 = o2;
	}
	
	@Override
	public Variable exe(Scope globalScope, Scope operationScope, Interrupt interrupt) {
		Variable v1 = o1.execute(globalScope, operationScope, interrupt);
		Variable v2 = o2.execute(globalScope, operationScope, interrupt);
		result.copyFrom(v1);
		result.divide(v2);
		return result;
	}
	
	@Override
	public String toString() {
		return o1 + " / " + o2;
	}
	
	@Override
	public String toString(int tab) {
		return o1.toString(tab) + " / " + o2.toString(tab);
	}
	
}
