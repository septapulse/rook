package io.septapulse.rook.daemon;

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

import io.septapulse.rook.api.exception.InitException;
import io.septapulse.rook.daemon.websocket.DaemonWebSocketCreator;

public class Daemon {

	private static final String DAEMON_HTML_DIR_NAME = "daemon/html";
	private static final String UI_DIR_NAME = "ui";
	
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
		
		File daemonHtmlDir = new File(platformDir, DAEMON_HTML_DIR_NAME);
		File platformUiDir = new File(platformDir, UI_DIR_NAME);
		File usrUiDir = new File(usrDir, UI_DIR_NAME);
		
		ResourceHandler daemonHandler = new ResourceHandler();
		daemonHandler.setDirectoriesListed(false);
		daemonHandler.setWelcomeFiles(new String[]{ "index.html" });
		daemonHandler.setResourceBase(daemonHtmlDir.getAbsolutePath());
	    ContextHandler daemonContext = new ContextHandler();
	    daemonContext.setContextPath("/");
	    daemonContext.setHandler(daemonHandler);
	    
		ResourceHandler platformHandler = new ResourceHandler();
		platformHandler.setDirectoriesListed(false);
		daemonHandler.setWelcomeFiles(new String[]{ "index.html" });
		platformHandler.setResourceBase(platformUiDir.getAbsolutePath());
	    ContextHandler platformContext = new ContextHandler();
	    platformContext.setContextPath("/ui/");
	    platformContext.setHandler(platformHandler);
	    
	    ResourceHandler usrHandler = new ResourceHandler();
	    usrHandler.setDirectoriesListed(false);
	    daemonHandler.setWelcomeFiles(new String[]{ "index.html" });
	    usrHandler.setResourceBase(usrUiDir.getAbsolutePath());
	    ContextHandler usrContext = new ContextHandler();
	    usrContext.setContextPath("/ui/");
	    usrContext.setHandler(usrHandler);
		
	    HandlerList handlerList = new HandlerList();
	    handlerList.setHandlers(new Handler[] { wsHandler, daemonContext, platformContext, usrContext, new DefaultHandler() });
	    
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
