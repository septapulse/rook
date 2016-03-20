package rook.api.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
public class FunctionManager {

	private final Map<String, Map<Integer, Function>> functions = new LinkedHashMap<>();
	
	public void addFunction(String name, int numParamters, Function function) throws CompilationException {
		Map<Integer, Function> m = functions.get(name);
		if(m == null) {
			m = new HashMap<>();
			functions.put(name, m);
		}
		if(m.containsKey(numParamters)) {
			throw new CompilationException("Duplicate function definition: " + 
					name + " with " + numParamters + " parameters");
		}
		m.put(numParamters, function);
	}
	
	public Function getFunction(String name, int numParameters) {
		Map<Integer, Function> m = functions.get(name);
		if(m != null) {
			return m.get(numParameters);
		}
		return null;
	}
	
	public List<Function> getFunctions() {
		List<Function> l = new ArrayList<>();
		for(Map.Entry<String, Map<Integer, Function>> e : functions.entrySet()) {
			l.addAll(e.getValue().values());
		}
		return l;
	}
}
