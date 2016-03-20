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
public class ConstantOperation extends Operation {

	private final Variable constant;
	
	public ConstantOperation(Variable constant) {
		this.constant = constant;
	}
	
	@Override
	public Variable exe(Scope globalScope, Scope operationScope, Interrupt interrupt) {
		return constant;
	}

	@Override
	public String toString() {
		return constant.toString();
	}
	
	@Override
	public String toString(int tab) {
		return toString();
	}
}
