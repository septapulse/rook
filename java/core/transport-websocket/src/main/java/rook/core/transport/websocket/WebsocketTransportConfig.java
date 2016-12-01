package rook.core.transport.websocket;

import rook.api.config.Configurable;

/**
 * Parsed configuration for a {@link WebsocketTransport}
 * 
 * @author Eric Thill
 *
 */
public class WebsocketTransportConfig {
	@Configurable(comment="URL of Websocket Server", defaultValue="ws://localhost:8080")
	private String url = "ws://localhost:8080";
	
	public String getUrl() {
		return url;
	}
	
	public WebsocketTransportConfig setUrl(String url) {
		this.url = url;
		return this;
	}

}
	
