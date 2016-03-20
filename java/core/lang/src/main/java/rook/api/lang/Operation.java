package rook.api.lang;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
public abstract class Operation {
	public final Variable execute(Scope globalScope, Scope operationScope, Interrupt interrupt) throws ExecutionException {
		if(interrupt.isInterrupted()) {
			throw new ExecutionException(new InterruptedException());
		}
		return exe(globalScope, operationScope, interrupt);
	}
	public abstract Variable exe(Scope globalScope, Scope operationScope, Interrupt interrupt) throws ExecutionException;
	public abstract String toString(int tab);
}
