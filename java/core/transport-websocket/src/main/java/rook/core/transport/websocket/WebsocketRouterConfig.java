package rook.core.transport.websocket;

import rook.api.config.Configurable;

/**
 * Parsed configuration for an {@link TcpRouter}
 * 
 * @author Eric Thill
 *
 */
public class WebsocketRouterConfig {
	@Configurable(comment = "Websocket Server Port", defaultValue = "8080")
	private int port = 8080;

	public int getPort() {
		return port;
	}

	public WebsocketRouterConfig setPort(int port) {
		this.port = port;
		return this;
	}
}
