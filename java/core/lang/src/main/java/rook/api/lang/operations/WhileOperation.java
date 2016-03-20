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
public class WhileOperation extends Operation {

	private final Operation condition;
	private final Operation op;
	
	public WhileOperation(Operation condition, Operation op) {
		this.condition = condition;
		this.op = op;
	}

	@Override
	public Variable exe(Scope globalScope, Scope operationScope, Interrupt interrupt) {
		while(condition.execute(globalScope, operationScope, interrupt).booleanValue()) {
			op.execute(globalScope, operationScope, interrupt);
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("while ").append(condition).append(" ").append(op);
		return sb.toString();
	}
	
	@Override
	public String toString(int tab) {
		StringBuilder sb = new StringBuilder();
		appendTab(sb, tab).append("while ").append(condition).append(" \n");
		sb.append(op.toString(tab));
		sb.append("\n");
		return sb.toString();
	}
	
	private StringBuilder appendTab(StringBuilder sb, int tab) {
		for(int i = 0; i < tab; i++) {
			sb.append("  ");
		}
		return sb;
	}
}
