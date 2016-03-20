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
public class ReturnOperation extends Operation {

	private final String variableName;
	
	public ReturnOperation(String variableName) {
		this.variableName = variableName;
	}
	
	@Override
	public Variable exe(Scope globalScope, Scope operationScope, Interrupt interrupt) {
		return operationScope.getVariable(variableName);
	}
	
	@Override
	public String toString() {
		return variableName;
	}
	
	@Override
	public String toString(int tab) {
		return variableName;
	}
	
}
