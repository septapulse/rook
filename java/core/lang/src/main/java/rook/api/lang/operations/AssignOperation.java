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
public class AssignOperation extends Operation {

	private final CharSequence variableName;
	private final Operation op;
	
	public AssignOperation(CharSequence variableName, Operation op) {
		this.variableName = variableName;
		this.op = op;
	}

	@Override
	public Variable exe(Scope globalScope, Scope operationScope, Interrupt interrupt) {
		Variable result = op.execute(globalScope, operationScope, interrupt);
		operationScope.setVariable(variableName, result);
		return result;
	}
	
	@Override
	public String toString() {
		return variableName + " = " + op;
	}
	
	@Override
	public String toString(int tab) {
		return variableName + " = " + op.toString(tab);
	}
}
