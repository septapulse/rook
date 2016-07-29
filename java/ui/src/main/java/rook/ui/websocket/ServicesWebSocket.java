package rook.ui.websocket;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import rook.ui.environment.Environment;
import rook.ui.environment.ServiceManager;
import rook.ui.websocket.message.ServiceRequest;
import rook.ui.websocket.message.ServiceResponse;

/**
 * WebSocket that handles service messages
 * 
 * @author Eric Thill
 *
 */
@WebSocket
public class ServicesWebSocket {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final ServiceManager serviceManager;
	private final Gson gson = new Gson();
	
	public ServicesWebSocket(Environment environment) {
		this.serviceManager = environment.getServiceManager();
	}
	
	@OnWebSocketMessage
    public void onText(Session session, String message) throws IOException
    {
		if(logger.isDebugEnabled()) {
			logger.debug("Received: " + message);
		}
		
		ServiceRequest req = gson.fromJson(message, ServiceRequest.class);
		
		ServiceResponse resp = new ServiceResponse();
		resp.setId(req.getId());
		resp.setSuccess(true);
		
		final String type = req.getType();
		switch(type) {
		case "get_packages":
			resp.setPackages(serviceManager.getPackages());
			break;
		case "get_package_info":
			resp.setPackageInfo(serviceManager.getPackageInfo(req.getPkg()));
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
