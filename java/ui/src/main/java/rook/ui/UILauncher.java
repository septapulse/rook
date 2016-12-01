package rook.ui;

import java.io.File;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rook.api.config.Arg;
import rook.api.config.Args;
import rook.ui.environment.Environment;
import rook.ui.websocket.RookWebSocketCreator;

/**
 * Main class for the Rook UI
 * 
 * @author Eric Thill
 *
 */
@Deprecated
public class UILauncher {
	
	private static final Logger logger = LoggerFactory.getLogger(UILauncher.class);
	
	public static void main(String... argsArr) throws Exception {
		Args args = Args.parse(argsArr,
				new Arg("rt", "router-type", false, false, false, "Startup Router Type - Default to no router"),
				new Arg("rc", "router-config", false, false, false, "Startup Router Config - Only required when router-type is defined"),
				new Arg("rp", "router-package", false, false, false, "Router UI Package [tcp,aeron,mqtt] - Only required when router-type is defined"),
				new Arg("tt", "transport-type", true, true, false, "Local Service Transport Type"),
				new Arg("tc", "transport-config", true, true, false, "Local Service Transport Config"),
				new Arg("p", "port", true, false, false, "Port for HTTP Server - Default=8080"),
				new Arg("h", "html", true, false, false, "Location of html files to host - Default=html"),
				new Arg("dp", "platform-dir", true, false, false, "Location of platform directory - Default=platform"),
				new Arg("du", "user-dir", true, false, false, "Location of user directory - Default=usr"),
				new Arg("dr", "run-dir", true, false, false, "Location of run directory - Default=run"),
				new Arg("j", "jre-dir", true, false, false, "Location of Java Runtime Environment - If not present, defaults to system installed java command"));
		if(args == null)
			return;
		Class<?> routerType = Class.forName(args.getValue("rt"));
		String routerConfig = args.getValue("rc");
		String routerPackage = args.getValue("rp");
		Class<?> transportType = Class.forName(args.getValue("tt"));
		String transportConfig = args.getValue("tc");
		int port = args.getValue("p") != null ? Integer.parseInt(args.getValue("p")) : 8080;
		File htmlDir = args.getValue("h") != null ? new File(args.getValue("h")) : new File("html");
		File platformDir = args.getValue("dp") != null ? new File(args.getValue("dp")) : new File("platform");
		File userDir = args.getValue("du") != null ? new File(args.getValue("du")) : new File("usr");
		File runDir = args.getValue("dr") != null ? new File(args.getValue("dr")) : new File("run");
		File jreDir = args.getValue("j") != null ? new File(args.getValue("j")) : null;
		userDir.mkdirs();
		runDir.mkdirs();
		
		UILauncher launcher = new UILauncher(routerType, routerConfig, routerPackage, transportType, transportConfig, 
				port, htmlDir, platformDir, userDir, runDir, jreDir);
		Runtime.getRuntime().addShutdownHook(new Thread(launcher::stop));
		launcher.start();
	}
	
	private final int port;
	private final File htmlDir;
	private final Environment environment;
	private Server server;
	
	/**
	 * Constructor
	 * @param routerType
	 * @param routerConfig
	 * @param routerPackage
	 * @param transportType
	 * @param transportConfig
	 * @param port
	 * @param htmlDir
	 * @param platformDir
	 * @param userDir
	 * @param runDir
	 * @param jreDir
	 * @throws Exception
	 */
	public UILauncher(Class<?> routerType, String routerConfig, String routerPackage, Class<?> transportType, String transportConfig, 
			int port, File htmlDir, File platformDir, File userDir, File runDir, File jreDir) throws Exception {
		this.port = port;
		this.htmlDir = htmlDir;
		this.environment = new Environment(
				routerType, routerConfig, routerPackage,
				transportType, transportConfig, 
				platformDir, userDir, runDir, jreDir);
	}
	
	/**
	 * Start the UI
	 * 
	 * @throws Exception
	 */
	public synchronized void start() throws Exception {
		environment.start();
		server = new Server(port);

		WebSocketHandler wsHandler = new WebSocketHandler() {
			@Override
			public void configure(WebSocketServletFactory factory) {
				factory.setCreator(new RookWebSocketCreator(environment));
			}
		};
		
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(true);
	    resourceHandler.setWelcomeFiles(new String[]{ "index.html" });
	    resourceHandler.setResourceBase(htmlDir.getAbsolutePath());
	    ContextHandler resourceContext = new ContextHandler();
	    resourceContext.setContextPath("/");
	    resourceContext.setHandler(resourceHandler);

	    HandlerList handlers = new HandlerList();
	    handlers.setHandlers(new Handler[] { wsHandler, resourceContext, new DefaultHandler() });
	    
	    logger.info("Starting Server on port " + port);
	    server.setHandler(handlers);
	    server.start();
	}
	
	public void stop() {
		if(server != null) {
			try {
				server.stop();
			} catch (Exception e) {
				logger.error("Couldn't stop jetty server", e);
			}
		}
		if(environment.getRuntimeManager() != null)
			environment.getRuntimeManager().stopForcibly();
	}
	
}
