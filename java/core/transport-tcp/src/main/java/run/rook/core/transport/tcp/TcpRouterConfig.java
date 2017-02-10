package run.rook.core.transport.tcp;

import run.rook.api.config.Configurable;

/**
 * Parsed configuration for an {@link TcpRouter}
 * 
 * @author Eric Thill
 *
 */
public class TcpRouterConfig {
	@Configurable(comment = "TCP Server Port", defaultValue = "9001")
	private int port = 9001;

	public int getPort() {
		return port;
	}

	public TcpRouterConfig setPort(int port) {
		this.port = port;
		return this;
	}
}
