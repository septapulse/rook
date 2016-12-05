package io.septapulse.rook.core.transport.websocket;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.septapulse.rook.api.Router;
import io.septapulse.rook.api.config.Configurable;
import io.septapulse.rook.api.exception.InitException;

/**
 * A {@link Router} that spins up a TCP Server/Broker
 * 
 * @author Eric Thill
 *
 */
@WebSocket
public class WebsocketRouter implements Router, WebSocketCreator {

	public static final String ROUTER_PROTOCOL = "router";

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Gson gson = new Gson();
	private final Map<String, Session> sessions = new LinkedHashMap<>();
	private final Map<Session, SessionInfo> sessionInfos = new LinkedHashMap<>();
	private final int port;
	private final Server server;

	public WebsocketRouter() {
		this.port = 0;
		this.server = null;
	}
	
	@Configurable
	public WebsocketRouter(WebsocketRouterConfig config) throws IOException {
		this.port = config.getPort();
		server = new Server(port);
		server.setHandler(new WebSocketHandler() {
			@Override
			public void configure(WebSocketServletFactory factory) {
				factory.setCreator(WebsocketRouter.this);
			}
		});
	}

	@Override
	public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
		for (String protocol : req.getSubProtocols()) {
			switch (protocol) {
			case ROUTER_PROTOCOL:
				resp.setAcceptedSubProtocol(protocol);
				return this;
			}
		}
		return null;
	}

	public synchronized void start() throws InitException {
		logger.info("Starting Server on port " + port);
		try {
			server.start();
		} catch (Exception e) {
			throw new InitException("Could not start " + getClass().getSimpleName(), e);
		}
	}

	public synchronized void stop() {
		try {
			server.stop();
		} catch (Exception e) {
			logger.error("Stop Failure", e);
		}
	}
	
	@OnWebSocketConnect
	public void onConnect(Session session) {
		session.setIdleTimeout(TimeUnit.MINUTES.toMillis(1));
	}

	@OnWebSocketMessage
    public void onText(Session session, String message) throws IOException {
		WebsocketMessage m = gson.fromJson(message, WebsocketMessage.class);
		switch(m.getType()) {
		case REGISTER:
			handleRegister(session, m);
			break;
		case ANNOUNCE:
			handleAnnounce(session, m);
			break;
		case BROADCAST:
			handleBroadcast(m);
			break;
		case JOIN:
			handleJoin(session, m);
			break;
		case LEAVE:
			handleLeave(session, m);
			break;
		case PROBE:
			handleProbe(m);
			break;
		case UNICAST:
			handleUnicast(m);
			break;
		case START_INCOGNITO_LISTEN:
			handleStartIncognitoListen(session, m);
			break;
		}
	}
	
	private void handleRegister(Session session, WebsocketMessage m) {
		// remove existing
		removeSession(session);
		// add session
		sessions.put(m.getFrom(), session);
		sessionInfos.put(session, new SessionInfo().setId(m.getFrom()));
	}
	
	private void handleAnnounce(Session session, WebsocketMessage m) {
		// forward to all services
		sendAll(m);
	}
	
	private void handleBroadcast(WebsocketMessage m) {
		for(Map.Entry<Session, SessionInfo> e : sessionInfos.entrySet()) {
			if(e.getValue().containsGroup(m.getGroup())) {
				send(e.getKey(), m);
			}
		}
	}
	
	private void handleJoin(Session session, WebsocketMessage m) {
		SessionInfo info = sessionInfos.get(session);
		if(info != null) {
			info.addGroup(m.getGroup());
			sendAll(m);
		}
	}
	
	private void handleLeave(Session session, WebsocketMessage m) {
		SessionInfo info = sessionInfos.get(session);
		if(info != null) {
			info.removeGroup(m.getGroup());
			sendAll(m);
		}
	}
	
	private void handleProbe(WebsocketMessage m) {
		sendAll(m);
	}
	
	private void handleUnicast(WebsocketMessage m) {
		Session s = sessions.get(m.getTo());
		if(s != null) {
			send(s, m);
		}
		for(Map.Entry<Session, SessionInfo> e : sessionInfos.entrySet()) {
			if(e.getValue().isIncognito()) {
				send(e.getKey(), m);
			}
		}
	}
	
	private void handleStartIncognitoListen(Session session, WebsocketMessage m) {
		SessionInfo info = sessionInfos.get(session);
		if(info != null) {
			info.setIncognito(true);
		}
	}

	private void sendAll(WebsocketMessage m) {
		String json = gson.toJson(m);
		for(Session s : sessions.values()) {
			send(s, json);
		}
	}
	
	private void send(Session s, WebsocketMessage m) {
		String json = gson.toJson(m);
		send(s, json);
	}
	
	private void send(Session s, String json) {
		synchronized (s.getRemote()) {
			try {
				s.getRemote().sendString(json);
			} catch (Throwable t) {
				logger.error("Session Exception", t);
				s.close();
			}
		}
	}

	@OnWebSocketClose
	public void onClose(Session session, int statusCode, String reason) {
		removeSession(session);
	}
	
	private void removeSession(Session session) {
		SessionInfo existingInfo = sessionInfos.remove(session);
		if (existingInfo != null) {
			sessions.remove(existingInfo.getId());
		}
	}

}
