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
public class PostIncrementOperation extends Operation {

	private final Variable ret = new Variable();
	private final CharSequence variableName;
	
	public PostIncrementOperation(CharSequence variableName) {
		this.variableName = variableName;
	}

	@Override
	public Variable exe(Scope globalScope, Scope operationScope, Interrupt interrupt) {
		Variable var = operationScope.getVariable(variableName);
		ret.copyFrom(var);
		var.set(var.longValue()+1);
		return ret;
	}
	
	@Override
	public String toString() {
		return variableName + "++";
	}
	
	@Override
	public String toString(int tab) {
		return toString();
	}
}
