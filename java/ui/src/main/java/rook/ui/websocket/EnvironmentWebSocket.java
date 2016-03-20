package rook.ui.websocket;

import java.io.IOError;
import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.google.gson.Gson;

import rook.ui.environment.Environment;
import rook.ui.websocket.message.LogMessage;
import rook.ui.websocket.message.EnvironmentRequest;

/**
 * WebSocket that handles environment messages (start, stop, etc)
 * 
 * @author Eric Thill
 *
 */
@WebSocket
public class EnvironmentWebSocket {
	
//	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Gson gson = new Gson();
	private final Environment environment;
	
	public EnvironmentWebSocket(Environment environment) {
		this.environment = environment;
	}
	
	@OnWebSocketMessage
    public void onText(final Session session, String message) throws IOException {
		EnvironmentRequest req = gson.fromJson(message, EnvironmentRequest.class);
		final String type = req.getType();
		switch(type) {
		case "start":
			environment.start(req.getCfg());
			break;
		case "stop":
			environment.stop();
			break;
		case "log":
			LogSender l = new LogSender(session);
			environment.log().addLogConsumer(l::send);
			break;
		}
    }
	
	private static class LogSender {
		private final Gson logGson = new Gson();
		private final Session s;
		public LogSender(Session s) {
			this.s = s;
		}
		public void send(String line) {
			LogMessage lm = new LogMessage();
			lm.setMessage(line);
			String msg = logGson.toJson(lm);
			if(s.isOpen()) {
				try {
					s.getRemote().sendString(msg);
				} catch(Exception e) {
					throw new IOError(new IOException("Closed", e));
				}
			} else {
				throw new IOError(new IOException("Closed"));
			}
		}
	}
	
}
