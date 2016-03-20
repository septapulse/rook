package rook.api.lang.operations;

import java.util.List;

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
public class ScopedOperation extends Operation {

	private final List<Operation> operations;
	private final boolean brackets;
	private final Scope scope = new Scope(); // this does not support concurrency
	
	public ScopedOperation(List<Operation> operations, boolean brackets) {
		this.operations = operations;
		this.brackets = brackets;
	}
	
	@Override
	public Variable exe(Scope globalScope, Scope operationScope, Interrupt interrupt) {
		scope.clear();
		scope.setParentScope(operationScope);
		
		Variable v = null;
		for(int i = 0; i < operations.size(); i++) {
			 v = operations.get(i).execute(globalScope, scope, interrupt);
		}
		return v;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(brackets) {
			sb.append("{ ");
		}
		for(int i = 0; i < operations.size(); i++) {
			if(i > 0) {
				sb.append(" ");
			}
			sb.append(operations.get(i).toString().trim());
		}
		if(brackets) {
			sb.append("}");
		}
		return sb.toString();
	}
	
	@Override
	public String toString(int tab) {
		StringBuilder sb = new StringBuilder();
		if(brackets) {
			appendTab(sb, tab).append("{ \n");
			tab++;
		}
		for(int i = 0; i < operations.size(); i++) {
			appendTab(sb, tab).append(operations.get(i).toString(tab).trim()).append("\n");
		}
		if(brackets) {
			tab--;
			appendTab(sb, tab).append("}");
		} else {
			sb.setLength(sb.length()-1);
		}
		return sb.toString();
	}
	
	private StringBuilder appendTab(StringBuilder sb, int tab) {
		for(int i = 0; i < tab; i++) {
			sb.append("  ");
		}
		return sb;
	}

}
