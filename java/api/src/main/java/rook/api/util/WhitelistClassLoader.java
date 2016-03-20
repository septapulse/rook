package rook.api.util;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Only loads classes from the underling ClassLoader when they are in
 * a given package. 
 * 
 * @author Eric Thill
 *
 */
public class WhitelistClassLoader extends ClassLoader {

	private final Set<String> packages = new LinkedHashSet<String>();
	private final ClassLoader classLoader;
	
	public WhitelistClassLoader(String... packages) {
		this.classLoader = getParent();
		for(String s : packages) {
			this.packages.add(s);
		}
	}
	
	public WhitelistClassLoader(ClassLoader classLoader, String... packages) {
		super(null);
		this.classLoader = classLoader;
		for(String s : packages) {
			this.packages.add(s);
		}
	}
	
	private boolean isWhitelisted(String name) {
		if(name.lastIndexOf('.') != -1) {
			for (String pkg : packages) {
				String classPkg = name.substring(0, name.lastIndexOf('.'));
				return classPkg.startsWith(pkg);
			}
		}
		return false;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if(isWhitelisted(name)) {
			return classLoader.loadClass(name);
		}
		throw new ClassNotFoundException(name);
	}
	
}
