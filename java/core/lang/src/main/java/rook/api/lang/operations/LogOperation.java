package rook.api.lang.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class LogOperation extends Operation {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public Variable exe(Scope globalScope, Scope operationScope, Interrupt interrupt) {
		Variable s = operationScope.getVariable("s");
		logger.info(s.toString());
		return s;
	}
	
	@Override
	public String toString() {
		return "log";
	}
	
	@Override
	public String toString(int tab) {
		return "log";
	}
	
}
