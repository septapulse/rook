package rook.ui.websocket;

import java.io.IOError;
import java.io.IOException;

import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.Gson;

import rook.ui.environment.LogSender;
import rook.ui.websocket.message.LogMessage;

@Deprecated
class WebSocketLogSender implements LogSender {
	private final Gson logGson = new Gson();
	private final Session s;
	private final long uid;

	public WebSocketLogSender(Session s, long uid) {
		this.s = s;
		this.uid = uid;
	}

	@Override
	public void send(String line) {
		LogMessage lm = new LogMessage().setMessage(line).setUid(uid);
		String msg = logGson.toJson(lm);
		if (s.isOpen()) {
			try {
				s.getRemote().sendString(msg);
			} catch (Exception e) {
				throw new IOError(new IOException("Closed", e));
			}
		} else {
			throw new IOError(new IOException("Closed"));
		}
	}

	@Override
	public boolean isOpen() {
		return s.isOpen();
	}
}