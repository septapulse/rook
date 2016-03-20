package rook.api.lang;

/**
 * Represents a compiled rook script
 * 
 * @author Eric Thill
 *
 */
public class Application {

	private final Interrupt interrupt = new Interrupt();
	private final FunctionManager functionManager;
	private final Operation main;
	
	public Application(FunctionManager functionManager, Operation main) {
		this.functionManager = functionManager;
		this.main = main;
	}
	
	public Variable execute() throws ExecutionException {
		return execute(new GlobalScope());
	}
	
	public Variable execute(GlobalScope globalScope) throws ExecutionException {
		try {
			return main.execute(globalScope.getScope(), globalScope.getScope(), interrupt);
		} catch(ExecutionException e) {
			throw e;
		} catch(Throwable t) {
			throw new ExecutionException(t);
		}
	}
	
	public void stop() {
		interrupt.interrupt();
	}
	
	@Override
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean prettyPrint) {
		StringBuilder sb = new StringBuilder();
		if(prettyPrint) {
			sb.append(main.toString(0));
			for(Function f : functionManager.getFunctions()) {
				sb.append(f.toString(0)).append("\n");
			}
		} else {
			sb.append(main.toString());
			for(Function f : functionManager.getFunctions()) {
				sb.append(f.toString()).append("\n");
			}
		}
		if(sb.length() > 0)
			sb.setLength(sb.length()-1);
		return sb.toString();
	}
}
