package rook.api.lang;

import java.util.List;

/**
 * A scoped function
 * 
 * @author Eric Thill
 *
 */
public class Function {

	private final Scope functionScope = new Scope(); // this does not support concurrency
	private final String functionName;
	private final List<String> parameters;
	private final Operation op;
	private final boolean invisible;
	
	public Function(String functionName, List<String> parameters, Operation op) {
		this(functionName, parameters, op, false);
	}
	
	public Function(String functionName, List<String> parameters, Operation op, boolean invisible) {
		this.functionName = functionName;
		this.parameters = parameters;
		this.op = op;
		this.invisible = invisible;
	}
	
	public Variable call(Scope globalScope, List<Variable> callingVariables, Interrupt interrupt) {
		functionScope.clear();
		functionScope.setParentScope(null);
		
		// set the variables in the function scope
		for(int i = 0; i < parameters.size(); i++) {
			String toName = parameters.get(i);
			Variable var = callingVariables.get(i);
			functionScope.setVariable(toName, var);
		}

		functionScope.setParentScope(globalScope);
		return op.execute(globalScope, functionScope, interrupt);
	}

	@Override
	public String toString() {
		if(invisible) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		appendFunctionHeader(sb).append(op.toString());
		return sb.toString();
	}

	public String toString(int tab) {
		if(invisible) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		appendFunctionHeader(sb).append(op.toString(tab));
		return sb.toString();
	}
	
	private StringBuilder appendFunctionHeader(StringBuilder sb) {
		sb.append("function ").append(functionName).append("(");
		for(int i = 0; i < parameters.size(); i++) {
			if(i > 0) {
				sb.append(", ");
			}
			sb.append(parameters.get(i));
		}
		sb.append(") ");
		return sb;
	}

}
