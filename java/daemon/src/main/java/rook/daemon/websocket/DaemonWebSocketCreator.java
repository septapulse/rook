package rook.daemon.websocket;

import java.io.File;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

import rook.core.transport.websocket.WebsocketRouter;

public class DaemonWebSocketCreator implements WebSocketCreator {

	private static final String PACKAGES_DIR = "packages"; 
	
	private final WebsocketRouter websocketRouter = new WebsocketRouter();
	private final PackageManager packageManager;
	
	public DaemonWebSocketCreator(File platformDir, File usrDir) {
		packageManager = new PackageManager(
				new File(platformDir, PACKAGES_DIR), 
				new File(usrDir, PACKAGES_DIR));
	}
	
	public void init() {
		packageManager.refresh();
	}
	
	@Override
	public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
		for (String protocol : req.getSubProtocols()) {
			switch (protocol) {
			case WebsocketRouter.ROUTER_PROTOCOL:
				resp.setAcceptedSubProtocol(protocol);
				return websocketRouter;
			case PackageManager.PACKAGE_MANAGER_PROTOCOL:
				resp.setAcceptedSubProtocol(protocol);
				return packageManager;
			}
		}
		return null;
	}

}
