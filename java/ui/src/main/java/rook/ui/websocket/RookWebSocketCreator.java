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
public class RookWebSocketCreator implements WebSocketCreator {

	private final IOWebSocket io;
	private final EnvironmentWebSocket env;
	private final ConfigWebSocket cfg;
	private final ScriptWebSocket script;
	
	public RookWebSocketCreator(Environment environment) {
		io = new IOWebSocket(environment);
		env = new EnvironmentWebSocket(environment);
		cfg = new ConfigWebSocket(environment);
		script = new ScriptWebSocket(environment);
	}
	
	@Override
	public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
		for (String protocol : req.getSubProtocols()) {
			switch (protocol) {
			case "io":
				resp.setAcceptedSubProtocol(protocol);
				return io;
			case "env":
				resp.setAcceptedSubProtocol(protocol);
				return env;
			case "cfg":
				resp.setAcceptedSubProtocol(protocol);
				return cfg;
			case "script":
				resp.setAcceptedSubProtocol(protocol);
				return script;
			}
		}
		return null;
	}

}
