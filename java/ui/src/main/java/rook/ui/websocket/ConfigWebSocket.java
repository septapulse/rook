package rook.ui.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

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
		this.configManager = environment.configManager();
	}
	
	@SuppressWarnings("unchecked")
	@OnWebSocketMessage
    public void onText(Session session, String message) throws IOException
    {
		ConfigRequest req = gson.fromJson(message, ConfigRequest.class);
		
		ConfigResponse resp = new ConfigResponse();
		resp.setId(req.getId());
		resp.setSuccess(true);
		
		final String type = req.getType();
		switch(type) {
		case "get_configs":
			resp.setCfgs(new ArrayList<String>(configManager.getConfigNames()));
			break;
		case "get_config":
			if(req.getName() != null) {
				resp.setCfg(gson.fromJson(configManager.getConfig(req.getName()), Map.class));
			} else {
				resp.setSuccess(false);
			}
			break;
		case "set_config":
			try {
				if(req.getCfg() != null) {
					configManager.setConfig(req.getName(), gson.toJson(req.getCfg()));
				} else {
					configManager.deleteConfig(req.getName());
				}
			} catch(IOException e) {
				logger.error("Could not set config: " + req.getName(), e);
				resp.setSuccess(false);
			}
			break;
		case "get_services":
			resp.setServices(new ArrayList<>(configManager.getServices()));
			break;
		case "get_template":
			try {
				if(req.getLibrary() != null && req.getName() != null) {
					resp.setTemplate(configManager.getConfigTemplate(req.getLibrary(), req.getName()));
				} else {
					resp.setSuccess(false);
				}
			} catch (Exception e) {
				logger.error("Could not load config template for " + req.getLibrary() + "/" + req.getName(), e);
				resp.setSuccess(false);
			}
			break;
		default:
			resp.setSuccess(false);
			break;
		}
		
		session.getRemote().sendString(gson.toJson(resp));
    }
	
}
