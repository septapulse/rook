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
public class GroupOperation extends Operation {

	private final Operation op;
	private final Scope scope = new Scope();
	
	public GroupOperation(Operation op) {
		this.op = op;
	}
	
	@Override
	public Variable exe(Scope globalScope, Scope operationScope, Interrupt interrupt) {
		scope.setParentScope(operationScope);
		return op.execute(globalScope, scope, interrupt);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(op);
		sb.append(")");
		return sb.toString();
	}
	
	@Override
	public String toString(int tab) {
		return toString();
	}

}
