package rook.api.lang.operations;

import rook.api.lang.ExecutionException;
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
public class SleepOperation extends Operation {

	@Override
	public Variable exe(Scope globalScope, Scope operationScope, Interrupt interrupt) throws ExecutionException {
		Variable millis = operationScope.getVariable("millis");
		final long end = System.currentTimeMillis()+millis.longValue();
		while(System.currentTimeMillis() < end) {
			if(interrupt.isInterrupted()) {
				throw new ExecutionException(new InterruptedException());
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				throw new ExecutionException(e);
			}
		}
		return millis;
	}
	
	@Override
	public String toString() {
		return "sleep";
	}
	
	@Override
	public String toString(int tab) {
		return "sleep";
	}
	
}
