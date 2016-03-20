package rook.api.lang;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * The global scope used by an {@link Application}
 * 
 * @author Eric Thill
 *
 */
public class GlobalScope {

	private final InternalScope internalScope = new InternalScope();

	public void setByReference(CharSequence name, Variable value) {
		// set by reference (allows for hooking into global variables with
		// outside logic)
		internalScope.setByReference(name, value);
	}

	public void setByValue(CharSequence name, Variable value) {
		internalScope.setByValue(name, value);
	}
	
	public void clear() {
		internalScope.clear();
	}
	
	public Variable getVariable(CharSequence name) {
		return internalScope.getVariable(name);
	}

	Scope getScope() {
		return internalScope;
	}

	/**
	 * Only called when the variable is changed by the script. GlobalScope's
	 * setByReference and setByValue methods will not trigger the consumer
	 * 
	 * @param c
	 */
	public void addVariableChangeConsumer(BiConsumer<CharSequence, Variable> c) {
		internalScope.addVariableChangeConsumer(c);
	}

	public void removeVariableChangeConsumer(BiConsumer<CharSequence, Variable> c) {
		internalScope.removeVariableChangeConsumer(c);
	}

	@Override
	public String toString() {
		return "GlobalScope [ " + internalScope.toString() + " ]";
	}
	
	private static class InternalScope extends Scope {
		private final Set<BiConsumer<CharSequence,Variable>> variableChangeConsumers = new LinkedHashSet<>();
		public synchronized void addVariableChangeConsumer(BiConsumer<CharSequence, Variable> c) {
			variableChangeConsumers.add(c);
		}
		public synchronized void removeVariableChangeConsumer(BiConsumer<CharSequence, Variable> c) {
			variableChangeConsumers.remove(c);
		}
		public synchronized void setByReference(CharSequence name, Variable value) {
			variables.put(name, value);
		}
		public synchronized void setByValue(CharSequence name, Variable value) {
			variables.put(name, value);
		}
		@Override
		public synchronized void setVariable(CharSequence name, Variable value) {
			super.setVariable(name, value);
			for(BiConsumer<CharSequence,Variable> c : variableChangeConsumers) {
				c.accept(name, value);
			}
		}
		@Override
		public void setParentScope(Scope parentScope) {
			throw new UnsupportedOperationException("Global Scope cannot have a parent");
		}
		@Override
		public synchronized Variable getVariable(CharSequence name) {
			return variables.get(name);
		}
	}

}
