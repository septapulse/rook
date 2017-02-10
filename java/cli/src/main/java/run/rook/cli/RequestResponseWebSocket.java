package run.rook.cli;

import java.io.IOException;
import java.util.concurrent.Future;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket(maxTextMessageSize = 64 * 1024)
public class RequestResponseWebSocket {

	private final String request;
	private final SettableFuture<String> response = new SettableFuture<>();
	private volatile Session session;
	
	public RequestResponseWebSocket(String request) {
		this.request = request;
	}
	
	public Future<String> getResponse() {
		return response;
	}
	
	@OnWebSocketConnect
	public void onConnect(Session session) {
		try {
			session.getRemote().sendString(request);
		} catch (IOException e) {
			response.fail(e);
		}
		this.session = session;
	}
	
	@OnWebSocketClose
	public void onClose(int statusCode, String reason) {
		response.cancel(true);
	}
	
	@OnWebSocketError
	public void onError(Throwable error) {
		response.fail(error);
	}

	@OnWebSocketMessage
	public void onMessage(String json) {
		response.set(json);
		Session session = this.session;
		if(session != null) {
			session.close();
			this.session = null;
		}
	}

}
