package rook.api.lang.operations;

import java.util.ArrayList;
import java.util.List;

import rook.api.lang.Function;
import rook.api.lang.FunctionManager;
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
public class FunctionCallOperation extends Operation {

	private final String functionName;
	private final List<Operation> input;
	private final FunctionManager functionManager;
	private final List<Variable> callingVariables; // this does not support concurrency
	private Function function;
	
	public FunctionCallOperation(String functionName, 
			List<Operation> input, 
			FunctionManager functionManager) {
		this.functionName = functionName;
		this.input = input;
		this.functionManager = functionManager;
		this.callingVariables = new ArrayList<>();
		for(int i = 0; i < input.size(); i++) {
			callingVariables.add(new Variable());
		}
	}
	
	@Override
	public Variable exe(Scope globalScope, Scope operationScope, Interrupt interrupt) {
		Function function = getFunction();
		for(int i = 0; i < input.size(); i++) {
			callingVariables.get(i).copyFrom(input.get(i).execute(globalScope, operationScope, interrupt));
		}
		return function.call(globalScope, callingVariables, interrupt);
	}
	
	private Function getFunction() {
		if(function == null) {
			function = functionManager.getFunction(functionName, input.size());
			if(function == null) {
				throw new RuntimeException("function '" + toString() + "' with " + input.size() + " parameter(s) is not defined");
			}
		}
		return function;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(functionName).append("(");
		for(int i = 0; i < input.size(); i++) {
			if(i > 0) {
				sb.append(", ");
			}
			sb.append(input.get(i));
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public String toString(int tab) {
		return toString();
	}
	
}
