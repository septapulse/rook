package rook.api;

import static rook.api.config.Arg.arg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rook.api.config.Args;
import rook.api.config.Config;
import rook.api.exception.InitException;
import rook.api.reflect.Instantiate;

/**
 * Utility and Main Method that uses reflection to launch a {@link Router} given
 * a type and configuration.
 * 
 * @author Eric Thill
 *
 */
public class RouterLauncher {

	public static void main(String... argsArr) {
		try {
			Args args = Args.parse(argsArr, arg("t", "type", true, true, false, "Fully Qualified Transport Class"),
					arg("c", "config", true, true, false, "Transport Configuration"));
			if (args == null)
				return;
			Class<?> type = Class.forName(args.getValue("t"));
			String config = args.getValue("c");
			RouterLauncher launcher = new RouterLauncher(type, config);
			launcher.launch();
			Runtime.getRuntime().addShutdownHook(new Thread(launcher::shutdown));
		} catch (Throwable t) {
			LoggerFactory.getLogger(ServiceLauncher.class).error("Could not launch Service", t);
			System.exit(-1);
		}
	}

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Class<?> type;
	private final String config;
	private volatile Router router;

	/**
	 * Constructor
	 * 
	 * @param type
	 *            The type of Router to instantiate
	 * @param config
	 *            The configuration @see {@link Config#parse(String)}
	 */
	public RouterLauncher(Class<?> type, String config) {
		this.type = type;
		this.config = config;
	}

	/**
	 * Start the router
	 * 
	 * @throws InitException
	 */
	public synchronized void launch() throws InitException {
		logger.info("Starting " + type.getName() + " config=" + config);
		if (router == null) {
			router = Instantiate.instantiate(type, config);
			router.start();
		}
		logger.info("Started");

	}

	/**
	 * Stop the router
	 */
	public synchronized void shutdown() {
		if (router != null) {
			router.stop();
		}
	}
}
