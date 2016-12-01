package rook.ui.websocket;

import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

import rook.ui.environment.Environment;

/**
 * Returns the correct {@link WebSocket} implementation based on the requested protocol
 * 
 * @author Eric Thill
 *
 */
@Deprecated
public class RookWebSocketCreator implements WebSocketCreator {

	private final IOWebSocket io;
	private final ServicesWebSocket services;
	private final RuntimeWebSocket runtime;
	private final ConfigWebSocket cfg;
//	private final ScriptWebSocket script;
	
	public RookWebSocketCreator(Environment environment) {
		io = new IOWebSocket(environment);
		services = new ServicesWebSocket(environment);
		runtime = new RuntimeWebSocket(environment);
		cfg = new ConfigWebSocket(environment);
	}
	
	@Override
	public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
		for (String protocol : req.getSubProtocols()) {
			switch (protocol) {
			case "io":
				resp.setAcceptedSubProtocol(protocol);
				return io;
			case "services":
				resp.setAcceptedSubProtocol(protocol);
				return services;
			case "runtime":
				resp.setAcceptedSubProtocol(protocol);
				return runtime;
			case "cfg":
				resp.setAcceptedSubProtocol(protocol);
				return cfg;
			}
		}
		return null;
	}

}
