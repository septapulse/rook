package rook.ui;

import java.io.File;
import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.Map;

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

import com.google.gson.Gson;

import rook.api.RookConfig;
import rook.ui.environment.Environment;
import rook.ui.websocket.RookWebSocketCreator;

/**
 * Main class for the Rook UI
 * 
 * @author Eric Thill
 *
 */
public class UIRunner {
	
	private static final Logger logger = LoggerFactory.getLogger(UIRunner.class);
	
	public static void main(String[] args) throws Exception {
		Map<String, String> config = parseArgs(args);
		logger.info("Parsed config: " + config);
		int port = config.containsKey("port") ? Integer.parseInt(config.get("port")) : 8080;
		File htmlDirectory = config.containsKey("html") ? new File(config.get("html")) : new File("html");
		String proxyConfigPath = config.containsKey("proxy") ? config.get("proxy") : "cfg/ui/proxy.json";
		File rootDirectory = config.containsKey("root") ? new File(config.get("root")) : new File(".");
		RookConfig proxyConfig = new Gson().fromJson(new FileReader(proxyConfigPath), RookConfig.class);
		new UIRunner(port, htmlDirectory, rootDirectory, proxyConfig).start();
	}
	
	private static Map<String, String> parseArgs(String[] args) {
		Map<String, String> config = new LinkedHashMap<String, String>();
		for(int i = 0; i < args.length; i++) {
			String a = args[i];
			if(a.startsWith("--")) {
				String key = a.substring(2);
				String value = args[++i];
				config.put(key, value);
			}
		}
		return config;
	}
	
	private final Environment environment;
	private final int port;
	private final File htmlDirectory;
	
	public UIRunner(int port, File htmlDirectory, File rootDirectory, RookConfig proxyConfig) throws Exception {
		this.port = port;
		this.htmlDirectory = htmlDirectory;
		this.environment = new Environment(rootDirectory, proxyConfig);
	}
	
	public void start() throws Exception {
		Server server = new Server(port);

		WebSocketHandler wsHandler = new WebSocketHandler() {
			@Override
			public void configure(WebSocketServletFactory factory) {
				factory.setCreator(new RookWebSocketCreator(environment));
			}
		};
		
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(true);
	    resourceHandler.setWelcomeFiles(new String[]{ "index.html" });
	    resourceHandler.setResourceBase(htmlDirectory.getAbsolutePath());
	    ContextHandler resourceContext = new ContextHandler();
	    resourceContext.setContextPath("/");
	    resourceContext.setHandler(resourceHandler);

	    HandlerList handlers = new HandlerList();
	    handlers.setHandlers(new Handler[] { wsHandler, resourceContext, new DefaultHandler() });
	    
	    logger.info("Starting Server on port " + port);
	    server.setHandler(handlers);
	    server.start();
	    server.join();
	}
	
}
