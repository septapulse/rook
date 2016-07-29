package rook.api.transport.tcp;

import rook.api.config.Configurable;

/**
 * Parsed configuration for a {@link TcpTransport}
 * 
 * @author Eric Thill
 *
 */
public class TcpTransportConfig {
	@Configurable(comment="Host of TCP Daemon", defaultValue="localhost")
	private String host = "localhost";
	@Configurable(comment="Port of TCP Daemon", defaultValue="9001")
	private int port = 9001;
	
	public String getHost() {
		return host;
	}
	
	public TcpTransportConfig setHost(String host) {
		this.host = host;
		return this;
	}
	
	public int getPort() {
		return port;
	}
	
	public TcpTransportConfig setPort(int port) {
		this.port = port;
		return this;
	}
}
	
