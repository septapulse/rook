package rook.ui.websocket;

import java.io.IOError;
import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import rook.api.lang.CompilationException;
import rook.ui.environment.Environment;
import rook.ui.websocket.message.LogMessage;
import rook.ui.websocket.message.ScriptRequest;
import rook.ui.websocket.message.ScriptResponse;

/**
 * WebSocket that handles script messages (compile, run, stop)
 * 
 * @author Eric Thill
 *
 */
@WebSocket
public class ScriptWebSocket {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Gson gson = new Gson();
	private final Environment environment;
	
	public ScriptWebSocket(Environment environment) {
		this.environment = environment;
	}

	@OnWebSocketMessage
    public void onText(Session session, String message) throws IOException
    {
		ScriptRequest req = gson.fromJson(message, ScriptRequest.class);
		
		ScriptResponse resp = new ScriptResponse();
		resp.setId(req.getId());
		resp.setSuccess(true);
		
		final String type = req.getType();
		switch(type) {
		case "save":
			environment.script().save(req.getCode());
			break;
		case "get":
			resp.setCode(environment.script().getCode());
			break;
		case "start":
			try {
				environment.script().stop();
				environment.script().compile();
				environment.script().start();
			} catch(CompilationException e) {
				logger.info("Script Compile Error", e);
				resp.setError(e.getMessage());
			}
			resp.setSuccess(false);
			break;
		case "stop":
			logger.info("Stopping Script");
			environment.script().stop();
			break;
		case "console":
			ConsoleSender l = new ConsoleSender(session);
			environment.script().addConsoleConsumer(l::send);
			return; // don't send success response
		default:
			resp.setSuccess(false);
			break;
		}
		
		session.getRemote().sendString(gson.toJson(resp));
    }
	
	private static class ConsoleSender {
		private final Gson logGson = new Gson();
		private final Session s;
		public ConsoleSender(Session s) {
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
