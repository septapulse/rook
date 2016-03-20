package rook.api.lang;

import java.util.HashMap;
import java.util.Map;

/**
 * A scope of variables
 * 
 * @author Eric Thill
 *
 */
public class Scope {

	protected Scope parentScope;
	protected Map<CharSequence, Variable> variables = new HashMap<>();
	
	public synchronized void setVariable(CharSequence name, Variable value) {
		// set by value
		Variable v = variables.get(name);
		if(v == null && parentScope != null) {
			v = parentScope.getVariable(name);
			if(v != null) {
				// variable in parent, set it there.
				parentScope.setVariable(name, value);
				return;
			}
		}
		
		if(v == null) { 
			// variable didn't exist anywhere. create it here.
			v = new Variable();
			variables.put(name, v);
		}
		// variable belongs to the scope. set it.
		v.copyFrom(value);
	}
	
	public synchronized void setParentScope(Scope parentScope) {
		this.parentScope = parentScope;
	}
	
	public synchronized Variable getVariable(CharSequence name) {
		Variable v = variables.get(name);
		if(v == null && parentScope != null) {
			v = parentScope.getVariable(name);
		}
		return v;
	}
	
	public synchronized void clear() {
		variables.clear();
	}
	
	@Override
	public String toString() {
		return "Scope [parentScope=" + parentScope + ", variables=" + variables + "]";
	}
}
