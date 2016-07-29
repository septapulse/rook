package rook.ui.websocket;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import rook.ui.environment.ConfigManager;
import rook.ui.environment.Environment;
import rook.ui.websocket.message.ConfigRequest;
import rook.ui.websocket.message.ConfigResponse;

/**
 * WebSocket that handles configuration messages
 * 
 * @author Eric Thill
 *
 */
@WebSocket
public class ConfigWebSocket {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final ConfigManager configManager;
	private final Gson gson = new Gson();
	
	public ConfigWebSocket(Environment environment) {
		this.configManager = environment.getConfigManager();
	}
	
	@OnWebSocketMessage
    public void onText(Session session, String message) throws IOException
    {
		if(logger.isDebugEnabled()) {
			logger.debug("Received: " + message);
		}
		ConfigRequest req = gson.fromJson(message, ConfigRequest.class);
		
		ConfigResponse resp = new ConfigResponse();
		resp.setId(req.getId());
		resp.setSuccess(true);
		
		final String type = req.getType();
		switch(type) {
		case "get_configs":
			resp.setCfgs(configManager.getServiceConfigs(req.getPkg(), req.getSid()));
			break;
		default:
			resp.setSuccess(false);
			break;
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug("Sending: " + gson.toJson(resp));
		}
		session.getRemote().sendString(gson.toJson(resp));
    }
	
}
