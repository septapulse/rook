package run.rook.api.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Able to parse command-line arguments using the given {@link Arg}s
 * 
 * @author Eric Thill
 *
 */
public class Args {

	/**
	 * Parse the given arguments using context from the given {@link Arg}
	 * metadata.
	 * 
	 * @param args
	 *            The arguments to parse
	 * @param metadata
	 *            Information about the args to parse
	 * @return The parsed arguments, or null if there was a problem parsing. If
	 *         called by a main method, if(args==null)System.exit(errCode);
	 *         should probably follow this method.
	 */
	public static Args parse(String[] args, Arg... metadata) {
		if (args.length == 1 && args[0].equals("-help")) {
			for (Arg md : metadata) {
				System.err.println(md);
			}
			return null;
		}
		Args result = new Args();
		for (int i = 0; i < args.length; i++) {
			String a = args[i];
			if (a.startsWith("--")) {
				// flag by name
				a = a.substring(2);
				for (Arg md : metadata) {
					if (a.equals(md.getName())) {
						String v = args[++i];
						put(result.valuesByFlag, md.getFlag(), v);
						put(result.valuesByName, md.getName(), v);
					}
				}
			} else if (a.startsWith("-")) {
				// flag
				a = a.substring(1);
				for (Arg md : metadata) {
					if (a.equals(md.getFlag())) {
						String v = args[++i];
						put(result.valuesByFlag, md.getFlag(), v);
						put(result.valuesByName, md.getName(), v);
					}
				}
			} else {
				// args
				result.args.add(a);
			}
		}
		boolean missingRequired = false;
		for (Arg md : metadata) {
			if (md.isRequired() && result.valuesByFlag.get(md.getFlag()) == null) {
				missingRequired = true;
				System.err.println("Missing required flag -" + md.getFlag() + " --" + md.getName());
			}
		}
		return missingRequired ? null : result;
	}

	private static void put(Map<String, List<String>> map, String k, String v) {
		List<String> l = map.get(k);
		if (l == null) {
			l = new ArrayList<>();
			map.put(k, l);
		}
		l.add(v);
	}

	private final Map<String, List<String>> valuesByFlag = new HashMap<>();
	private final Map<String, List<String>> valuesByName = new HashMap<>();
	private final List<String> args = new ArrayList<>();

	private Args() {

	}

	/**
	 * Get value by flag
	 * 
	 * @param flag
	 *            the flag
	 * @return the value, or null if it didn't exist
	 */
	public String getValue(String flag) {
		List<String> l = valuesByFlag.get(flag);
		if (l == null || l.size() == 0) {
			return null;
		} else {
			return l.get(0);
		}
	}

	/**
	 * Get value by long-hand name
	 * 
	 * @param flag
	 *            the long-hand name
	 * @return the value, or null if it didn't exist
	 */
	public String getValueByName(String name) {
		List<String> l = valuesByName.get(name);
		if (l == null || l.size() == 0) {
			return null;
		} else {
			return l.get(0);
		}
	}

	/**
	 * Get values list by flag
	 * 
	 * @param flag
	 *            the flag
	 * @return the collection of values, never null
	 */
	public List<String> getValues(String flag) {
		List<String> l = valuesByFlag.get(flag);
		if (l == null || l.size() == 0) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableList(l);
		}
	}

	/**
	 * Get values list by long-hand name
	 * 
	 * @param name
	 *            the long-hand name
	 * @return the collection of values, never null
	 */
	public List<String> getValuesByName(String name) {
		List<String> l = valuesByName.get(name);
		if (l == null || l.size() == 0) {
			return Collections.emptyList();
		} else {
			return Collections.unmodifiableList(l);
		}
	}

	@Override
	public String toString() {
		return "Arguments [values=" + valuesByFlag + ", args=" + args + "]";
	}

}
