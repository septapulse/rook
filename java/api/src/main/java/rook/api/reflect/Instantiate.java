package rook.api.reflect;

import java.lang.reflect.Constructor;

import com.google.gson.Gson;

import rook.api.config.Config;
import rook.api.config.Configurable;

/**
 * Utility methods to instantiate classes given a type and configuration.
 * 
 * @author Eric Thill
 *
 */
public class Instantiate {

	/**
	 * Instantiate the given type with the given config
	 * 
	 * @param type
	 *            The fully qualified class to instantiate
	 * @param config
	 *            The configuration @see {@link Config#parse(String)}
	 * @return The instantiated instance
	 */
	public static <T> T instantiate(String type, String config) {
		try {
			return instantiate(Class.forName(type), config);
		} catch (InstantiateException e) {
			throw e;
		} catch (Throwable t) {
			throw new InstantiateException("Could not instantiate type " + t, t);
		}
	}

	/**
	 * Instantiate the given class with the given config
	 * 
	 * @param c
	 *            The class to instantiate
	 * @param config
	 *            The configuration @see {@link Config#parse(String)}
	 * @return The instantiated instance
	 */
	@SuppressWarnings("unchecked")
	public static <T> T instantiate(Class<?> c, String config) {
		try {
			config = Config.parse(config);
			Constructor<?> constructor = getConfigurableConstructor(c);
			if (constructor != null) {
				if (constructor.getParameterCount() == 0) {
					// default constructor: nothing to deserialize. Instantiate
					// and return.
					return (T) constructor.newInstance();
				} else if (constructor.getParameterCount() == 1
						&& constructor.getParameterTypes()[0].equals(String.class)) {
					// configuration type is string: just pass it in.
					// Instantiate and return.
					return (T) constructor.newInstance(config);
				} else if (constructor.getParameterCount() == 1) {
					// use GSON() to parse JSON
					Object configObj = new Gson().fromJson(config, constructor.getParameterTypes()[0]);
					return (T) constructor.newInstance(configObj);
				}
			}
			throw new InstantiateException(c.getName()
					+ " does not specify a valid constructor. Did you forget the @Configurable annotation?");
		} catch (Throwable t) {
			throw new InstantiateException("Could not instantiate " + c.getName(), t);
		}
	}

	/**
	 * Searched for the configurable constructor to use for the given class.
	 * Order of priority is as follows:<br>
	 * 1. The first constructor found with the {@link Configurable} annotation
	 * that has 0 or 1 arguments.<rb> 2. The constructor that takes a single
	 * {@link String} as an argument. 3. The default constructor
	 * 
	 * @param c
	 *            The class to search
	 * @return The constructor used to instantiate it
	 */
	public static Constructor<?> getConfigurableConstructor(Class<?> c) {
		for (Constructor<?> con : c.getConstructors()) {
			if (con.isAnnotationPresent(Configurable.class) && con.getParameterCount() <= 1)
				return con;
		}
		for (Constructor<?> con : c.getConstructors()) {
			if (con.getParameterCount() == 1 && con.getParameterTypes()[0].equals(String.class))
				return con;
		}
		for (Constructor<?> con : c.getConstructors()) {
			if (con.getParameterCount() == 0)
				return con;
		}
		return null;
	}

}
