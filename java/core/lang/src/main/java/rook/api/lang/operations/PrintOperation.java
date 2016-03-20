package rook.api.lang.operations;

import java.util.function.Consumer;

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
public class PrintOperation extends Operation {

	private final Consumer<String> consoleLogger;
	
	public PrintOperation(Consumer<String> consoleLogger) {
		this.consoleLogger = consoleLogger;
	}
	
	@Override
	public Variable exe(Scope globalScope, Scope operationScope, Interrupt interrupt) {
		Variable s = operationScope.getVariable("s");
		consoleLogger.accept(s.toString());
		return s;
	}
	
	@Override
	public String toString() {
		return "print";
	}
	
	@Override
	public String toString(int tab) {
		return "print";
	}
	
}
