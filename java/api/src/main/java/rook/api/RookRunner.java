package rook.api;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import rook.api.config.ConfigurableConstructor;
import rook.api.config.ServiceConfig;
import rook.api.router.disruptor.DisruptorRouter;
import rook.api.util.InstantiateException;
import rook.api.util.PriorityClassLoader;
import rook.api.util.WhitelistClassLoader;

/**
 * Instantiates and starts a Rook Environment.
 * 
 * @author Eric Thill
 *
 */
public class RookRunner {

	private static final String[] PARENT_FIRST_PACKAGES = new String[] { "rook.api", "org.slf4j" };
	private static final String DEFAULT_ROUTER_TYPE = DisruptorRouter.class.getName();
	private final Logger logger = LoggerFactory.getLogger(RookRunner.class);

	/**
	 * Entry Point
	 * 
	 * @param args
	 *            args[0]: Path to JSON configuration
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.err.println("Use: RookRunner <path_to_json_config>");
			System.exit(-1);
		}

		// Load primary config
		RookConfig config = new Gson().fromJson(new FileReader(args[0]), RookConfig.class);

		// Load extra configs (services only)
		for (int i = 1; i < args.length; i++) {
			RookConfig secondary = new Gson().fromJson(new FileReader(args[i]), RookConfig.class);
			config.getServices().putAll(secondary.getServices());
		}

		new RookRunner().instantiate(config).start();
	}

	/**
	 * Instantiates a Rook Environment given a {@link RookConfig} object.
	 * 
	 * @param config
	 *            The Configuration
	 * @return The Router that connects and manages all of the services
	 * @throws InitException
	 *             When the environment could not be instantiated
	 */
	public Router instantiate(RookConfig config) throws InitException {
		try {
			ServiceConfig routerConfig = config.getRouter();
			if(routerConfig == null || routerConfig.getType() == null) {
				routerConfig = new ServiceConfig();
				routerConfig.setType(DEFAULT_ROUTER_TYPE);
			}
			logger.info("Instantiating Router. config=" + routerConfig);
			Router router = instantiateService(routerConfig, Router.class);
			
			for (Map.Entry<String, ServiceConfig> e : config.getServices().entrySet()) {
				String serviceName = e.getKey();
				logger.info("Instantiating " + serviceName + ". config=" + e.getValue());
				Service service = instantiateService(e.getValue(), Service.class);
				router.addService(RID.create(serviceName), service);
			}
			return router;
		} catch (Throwable t) {
			throw new InitException("Could not instantiate Rook Environment", t);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T instantiateService(ServiceConfig cfg, Class<T> returnType) throws Exception {
		ClassLoader classLoader = parseClassloader(cfg.getClasspath());

		try {
			Class<T> c = (Class<T>) classLoader.loadClass(cfg.getType());
			Constructor<T> constructor = getConfigurableConstructor(c);
			
			if(constructor.getParameterTypes().length == 0) {
				// no configurable constructor found: try the default constructor
				return c.newInstance();
			} else if(constructor.getParameterTypes()[0] == Map.class) {
				Object param = cfg.getConfig() != null ? cfg.getConfig() : Collections.emptyMap();
				return constructor.newInstance(param);
			} else if(constructor.getParameterTypes()[0] == Properties.class) {
				Properties props = new Properties();
				if(cfg.getConfig() != null) {
					props.putAll(cfg.getConfig());
				}
				return constructor.newInstance(props);
			} else {
				// Non-Properties Configuration was found, use Gson to parse
				String json = new Gson().toJson(cfg.getConfig(), Map.class);
				Object configuration = new Gson().fromJson(json, constructor.getParameterTypes()[0]);
				return constructor.newInstance(configuration);
			}
		} catch (Throwable t) {
			throw new InstantiateException("Could not instantiate from " + cfg, t);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Constructor<T> getConfigurableConstructor(Class<T> c) {
		// look for an @Configuration constructor with the correct number of arguments
		for (Constructor<?> constructor : c.getConstructors()) {
			if (constructor.isAnnotationPresent(ConfigurableConstructor.class)) {
				if (constructor.getParameterTypes().length == 1) {
					return (Constructor<T>) constructor;
				}
			}
		}
		
		// look for the first constructor with the right number of arguments
		for (Constructor<?> constructor : c.getConstructors()) {
			if (constructor.getParameterTypes().length == 1) {
				return (Constructor<T>) constructor;
			}
		}
		
		return null;
	}

	private static ClassLoader parseClassloader(String classpathProp) throws MalformedURLException {
		if (classpathProp == null) {
			return RookRunner.class.getClassLoader();
		} else {
			ClassLoader parent = RookRunner.class.getClassLoader();
			return new PriorityClassLoader(
					new WhitelistClassLoader(parent, PARENT_FIRST_PACKAGES),
					new URLClassLoader(parseClasspath(classpathProp)),
					parent
					);
		}
	}

	private static URL[] parseClasspath(String prop) throws MalformedURLException {
		List<URL> jars = new ArrayList<>();
		String[] arr = prop.split(",");
		for (String s : arr) {
			if (s.endsWith(".jar")) {
				jars.add(new File(s).toURI().toURL());
			} else if (new File(s).isDirectory()) {
				File dir = new File(s);
				File[] files = dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".jar");
					}
				});
				for (File f : files) {
					jars.add(f.toURI().toURL());
				}
			}
		}
		if (jars.size() == 0) {
			throw new IllegalArgumentException(prop + " does not contain any jars");
		}
		return jars.toArray(new URL[jars.size()]);
	}

}
