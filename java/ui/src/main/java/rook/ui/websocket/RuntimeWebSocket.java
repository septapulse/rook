package rook.ui.websocket;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import rook.ui.environment.Environment;
import rook.ui.environment.LogSender;
import rook.ui.environment.RuntimeManager;
import rook.ui.websocket.message.RuntimeRequest;
import rook.ui.websocket.message.RuntimeResponse;
import rook.ui.websocket.message.ProcessRunInfo;

/**
 * WebSocket that handles runtime messages
 * 
 * @author Eric Thill
 *
 */
@WebSocket
public class RuntimeWebSocket {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final RuntimeManager runtimeManager;
	private final Gson gson = new Gson();
	
	public RuntimeWebSocket(Environment environment) {
		this.runtimeManager = environment.getRuntimeManager();
	}
	
	@OnWebSocketMessage
    public void onText(Session session, String message) throws IOException, InterruptedException
    {
		if(logger.isDebugEnabled()) {
			logger.debug("Received: " + message);
		}
		
		RuntimeRequest req = gson.fromJson(message, RuntimeRequest.class);
		
		RuntimeResponse resp = new RuntimeResponse();
		resp.setId(req.getId());
		resp.setSuccess(true);
		
		ProcessRunInfo startRunInfo;
		final String type = req.getType();
		switch(type) {
		case "get_running":
			resp.setRunning(runtimeManager.getRunningServices());
			break;
		case "start_service":
			startRunInfo = runtimeManager.startService(req.getPkg(), req.getSid(), req.getCfg());
			resp.setInstance(startRunInfo);
			resp.setSuccess(startRunInfo != null);
			break;
		case "start_bridge":
			startRunInfo = runtimeManager.startTransportBridge(req.getPkg(), req.getSid(), req.getCfg());
			resp.setInstance(startRunInfo);
			resp.setSuccess(startRunInfo != null);
			break;
		case "start_router":
			startRunInfo = runtimeManager.startRouter(req.getPkg(), req.getSid(), req.getCfg());
			resp.setInstance(startRunInfo);
			resp.setSuccess(startRunInfo != null);
			break;
		case "stop":
			ProcessRunInfo stopRunInfo = runtimeManager.stop(req.getUid());
			resp.setInstance(stopRunInfo);
			resp.setSuccess(stopRunInfo != null);
			break;
		case "open_log_stream":
			LogSender logSender = new WebSocketLogSender(session, req.getUid());
			resp.setSuccess(runtimeManager.addLogConsumer(req.getUid(), logSender));
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
