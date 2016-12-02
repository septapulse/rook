package rook.daemon;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

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

import rook.api.exception.InitException;
import rook.daemon.websocket.DaemonWebSocketCreator;

public class Daemon {

	private static final String HTML_DIR_NAME = "html";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public static void main(String[] args) throws IOException, InitException {
		Reader r = null;
		try {
			int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
			new Daemon(port).start();
		} finally {
			if(r != null) {
				r.close();
			}
		}
	}

	private final int port;
	private final Server server;
	private final DaemonWebSocketCreator websocketCreator;
	
	public Daemon(int port) {
		this(
				port, 
				new File("platform"),
				new File("usr")
		);
	}
	public Daemon(int port, File platformDir, File usrDir) {
		this.port = port;
		
		websocketCreator = new DaemonWebSocketCreator(platformDir, usrDir);
		
		WebSocketHandler wsHandler = new WebSocketHandler() {
			@Override
			public void configure(WebSocketServletFactory factory) {
				factory.setCreator(websocketCreator);
			}
		};
		
		ResourceHandler platformHandler = new ResourceHandler();
		platformHandler.setDirectoriesListed(true);
		platformHandler.setWelcomeFiles(new String[]{ "index.html" });
		platformHandler.setResourceBase(new File(platformDir, HTML_DIR_NAME).getAbsolutePath());
	    ContextHandler platformContext = new ContextHandler();
	    platformContext.setContextPath("/");
	    platformContext.setHandler(platformHandler);
	    
	    ResourceHandler usrHandler = new ResourceHandler();
	    usrHandler.setDirectoriesListed(true);
	    usrHandler.setResourceBase(new File(usrDir, HTML_DIR_NAME).getAbsolutePath());
	    ContextHandler usrContext = new ContextHandler();
	    usrContext.setContextPath("/");
	    usrContext.setHandler(usrHandler);
		
	    HandlerList handlerList = new HandlerList();
	    handlerList.setHandlers(new Handler[] { wsHandler, platformContext, usrContext, new DefaultHandler() });
	    
	    server = new Server(port);
	    server.setHandler(handlerList);
	}
	
	public void start() throws InitException {
		logger.info("Initializing WebSocketCreator");
		websocketCreator.init();
		logger.info("Starting Server on port " + port);
		try {
			server.start();
		} catch (Exception e) {
			throw new InitException("Could not start " + getClass().getSimpleName(), e);
		}
		logger.info("Started");
	}
	
	public void stop() throws Exception {
		server.stop();
	}
}
