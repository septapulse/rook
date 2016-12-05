package io.septapulse.rook.api;

import static io.septapulse.rook.api.config.Arg.arg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.septapulse.rook.api.config.Args;
import io.septapulse.rook.api.config.Config;
import io.septapulse.rook.api.exception.InitException;
import io.septapulse.rook.api.reflect.Instantiate;
import io.septapulse.rook.api.transport.ControllableTransport;
import io.septapulse.rook.api.util.Sleep;

/**
 * Utility and Main Method that uses reflection to launch a {@link Service}
 * given a type and configuration.
 * 
 * @author Eric Thill
 *
 */
public class ServiceLauncher {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(String... argsArr) {
		try {
			Args args = Args.parse(argsArr, arg("id", "service-id", true, true, false, "Service ID"),
					arg("st", "service-type", true, true, false, "Fully Qualified Service Class"),
					arg("sc", "service-config", true, true, false, "Service Configuration"),
					arg("tt", "transport-type", true, true, false, "Fully Qualified Transport Class"),
					arg("tc", "transport-config", true, true, false, "Transport Configuration"));
			if (args == null)
				return;
			RID serviceId = RID.create(args.getValue("id"));
			Class<?> serviceType = Class.forName(args.getValue("st"));
			String serviceConfig = args.getValue("sc");
			Class<?> transportType = Class.forName(args.getValue("tt"));
			String transportConfig = args.getValue("tc");
			ServiceLauncher launcher = new ServiceLauncher(serviceId, serviceType, serviceConfig, transportType,
					transportConfig);
			launcher.launch();
			Runtime.getRuntime().addShutdownHook(new Thread(launcher::shutdown));
		} catch (Throwable t) {
			LoggerFactory.getLogger(ServiceLauncher.class).error("Could not launch Service", t);
			System.exit(-1);
		}
	}

	private final RID serviceId;
	private final Class<?> serviceType;
	private final String serviceConfig;
	private final Class<?> transportType;
	private final String transportConfig;
	private Service service;
	private ControllableTransport transport;

	/**
	 * Constructor
	 * 
	 * @param serviceId
	 *            {@link RID} to assign to the service, which is used by other
	 *            services to address this service.
	 * @param serviceType
	 *            The type of service to instantiate
	 * @param serviceConfig
	 *            The service configuration @see {@link Config#parse(String)}
	 * @param transportType
	 *            The type of transport to instantiate
	 * @param transportConfig
	 *            The transport configuration @see {@link Config#parse}
	 */
	public ServiceLauncher(RID serviceId, Class<?> serviceType, String serviceConfig, Class<?> transportType,
			String transportConfig) {
		this.serviceId = serviceId;
		this.serviceType = serviceType;
		this.serviceConfig = serviceConfig;
		this.transportType = transportType;
		this.transportConfig = transportConfig;
	}

	/**
	 * Start the transport and the router
	 * @throws InitException
	 */
	public synchronized void launch() throws InitException {
		logger.info("Creating " + transportType.getSimpleName());
		transport = Instantiate.instantiate(transportType, transportConfig);
		transport.setServiceId(serviceId);
		transport.setExceptionHandler(this::handleException);
		transport.start();

		logger.info("Creating " + serviceType.getSimpleName());
		service = Instantiate.instantiate(serviceType, serviceConfig);
		service.setTransport(transport);
		Sleep.trySleep(500);

		logger.info("Starting " + serviceType.getSimpleName());
		service.init();

		logger.info("Started");
	}

	public synchronized void shutdown() {
		if (service != null) {
			service.shutdown();
			service = null;
		}
		if (transport != null) {
			transport.shutdown();
		}
	}

	private void handleException(String error, Throwable t) {
		if (t == null) {
			logger.error(error);
		} else {
			t.printStackTrace();
			logger.error(error, t);
		}
		System.exit(-1);
	}

}
