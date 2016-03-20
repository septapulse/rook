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
public class IfOperation extends Operation {

	private final Operation condition;
	private final Operation thenOp;
	private final Operation elseOp;
	
	public IfOperation(Operation condition, Operation thenOp, Operation elseOp) {
		this.condition = condition;
		this.thenOp = thenOp;
		this.elseOp = elseOp;
	}

	@Override
	public Variable exe(Scope globalScope, Scope operationScope, Interrupt interrupt) {
		if(condition.execute(globalScope, operationScope, interrupt).booleanValue()) {
			return thenOp.execute(globalScope, operationScope, interrupt);
		} else if(elseOp != null) {
			return elseOp.execute(globalScope, operationScope, interrupt);
		} else {
			return null;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("if ").append(condition).append(" ").append(thenOp).append(" ");
		if(elseOp != null) {
			sb.append(" else ").append(elseOp).append(" ");
		}
		return sb.toString();
	}
	
	@Override
	public String toString(int tab) {
		StringBuilder sb = new StringBuilder();
		appendTab(sb, tab).append("if ").append(condition).append("\n");
		sb.append(thenOp.toString(tab));
		if(elseOp != null) {
			sb.append(" else ").append(elseOp.toString(tab));
		}
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
