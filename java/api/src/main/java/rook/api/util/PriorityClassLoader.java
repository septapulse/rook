package rook.api.util;

import java.util.Arrays;

/**
 * Attempts to load from the underlying ClassLoaders in the given order
 * 
 * @author Eric Thill
 *
 */
public class PriorityClassLoader extends ClassLoader {

	private final ClassLoader[] classLoaders;
	
	public PriorityClassLoader(ClassLoader... classLoaders) {
		super(null);
		this.classLoaders = Arrays.copyOf(classLoaders, classLoaders.length);
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		for(ClassLoader cl : classLoaders) {
			try {
				return cl.loadClass(name);
			} catch(Throwable t) {
				// continue
			}
		}
		throw new ClassNotFoundException(name);
	}
}
