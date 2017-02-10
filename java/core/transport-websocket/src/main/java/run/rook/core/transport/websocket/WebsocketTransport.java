package run.rook.core.transport.websocket;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import run.rook.api.RID;
import run.rook.api.config.Configurable;
import run.rook.api.exception.ExceptionHandler;
import run.rook.api.exception.InitException;
import run.rook.api.transport.AnnounceTransport;
import run.rook.api.transport.BroadcastTransport;
import run.rook.api.transport.ControllableTransport;
import run.rook.api.transport.GrowableBuffer;
import run.rook.api.transport.Transport;
import run.rook.api.transport.UnicastTransport;
import run.rook.api.transport.simple.MessageType;
import run.rook.api.transport.simple.Publisher;
import run.rook.api.transport.simple.SimpleAnnounceTransport;
import run.rook.api.transport.simple.SimpleBroadcastTransport;
import run.rook.api.transport.simple.SimpleUnicastTransport;

/**
 * A {@link Transport} implementation that uses a {@link Socket} to communicate
 * with a {@link WebsocketRouter}
 * 
 * @author Eric Thill
 *
 */
public class WebsocketTransport implements ControllableTransport {

	private static final int CONNECT_TIMEOUT_SECONDS = 5;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final Gson gson = new Gson();
	private final String url;
	private Session session;
	private SimpleAnnounceTransport announceTransport;
	private SimpleBroadcastTransport bcastTransport;
	private SimpleUnicastTransport ucastTransport;
	private RID serviceId;
	private ExceptionHandler exceptionHandler;
	private boolean respondToProbes = true;
	private JettyWebSocket socket;
	
	@Configurable
	public WebsocketTransport(WebsocketTransportConfig config) {
		url = config.getUrl();
	}

	@Override
	public void setServiceId(RID serviceId) {
		this.serviceId = serviceId;
	}

	@Override
	public void setExceptionHandler(ExceptionHandler h) {
		this.exceptionHandler = h;
	}

	@Override
	public void setRespondToProbes(boolean respondToProbes) {
		this.respondToProbes = respondToProbes;
	}

	@Override
	public synchronized void start() throws InitException {
		if (running.compareAndSet(false, true)) {
			try {
				logger.info("Connecting to " + url);
				WebSocketClient client = new WebSocketClient();
				socket = new JettyWebSocket();
	            client.start();
	            URI url = new URI(this.url);
	            ClientUpgradeRequest request = new ClientUpgradeRequest();
	            request.setSubProtocols(WebsocketRouter.ROUTER_PROTOCOL);
	            Future<Session> connectFuture = client.connect(socket,url,request);
	            session = connectFuture.get(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
	            if(session == null) {
	            	throw new IOException("Could not connect to " + url);
	            }
				logger.info("Connected");
				
				WebsocketPublisher publisher = new WebsocketPublisher(socket);
				announceTransport = new SimpleAnnounceTransport(serviceId, publisher, respondToProbes);
				bcastTransport = new SimpleBroadcastTransport(serviceId, publisher);
				ucastTransport = new SimpleUnicastTransport(serviceId, publisher, this::onStartIncognitoListen);
				
				if (respondToProbes)
					announceTransport.incognito_announce(serviceId);
				
				logger.info("Started");
			} catch (Throwable t) {
				throw new InitException("Could not start transport", t);
			}
		}
	}
	
	private void onStartIncognitoListen() {
		WebsocketMessage m = new WebsocketMessage(WebsocketMessageType.START_INCOGNITO_LISTEN, serviceId.toString(), null, null, null);
		String json = gson.toJson(m);
		try {
			socket.send(json);
		} catch (IOException e) {
			logger.error("START_INCOGNITO_LISTEN Failure", e);
		}
	}

	@Override
	public synchronized void shutdown() {
		if (running.compareAndSet(true, false)) {
			session.close();
		}
	}

	@Override
	public AnnounceTransport announce() {
		return announceTransport;
	}

	@Override
	public BroadcastTransport bcast() {
		return bcastTransport;
	}

	@Override
	public UnicastTransport ucast() {
		return ucastTransport;
	}
	
	private final class WebsocketPublisher implements Publisher {

		private final JettyWebSocket socket;
		
		public WebsocketPublisher(JettyWebSocket socket) {
			this.socket = socket;
		}
		
		@Override
		public void publish(MessageType type, RID from, RID to, RID group, GrowableBuffer msg) {
			WebsocketMessage m = new WebsocketMessage(mapType(type), 
					from == null ? null : from.toString(), 
					to == null ? null : to.toString(), 
					group == null ? null : group.toString(), 
					msg == null ? null : msg.toBase64());
			String json = gson.toJson(m);
			try {
				socket.send(json);
			} catch (IOException e) {
				e.printStackTrace();
				exceptionHandler.error("Publish Failure", e);
			}
		}
		
		private WebsocketMessageType mapType(MessageType type) {
			switch(type) {
			case ANNOUNCE:
				return WebsocketMessageType.ANNOUNCE;
			case BCAST_JOIN:
				return WebsocketMessageType.JOIN;
			case BCAST_LEAVE:
				return WebsocketMessageType.LEAVE;
			case BCAST_MESSAGE:
				return WebsocketMessageType.BROADCAST;
			case PROBE:
				return WebsocketMessageType.PROBE;
			case UCAST_MESSAGE:
				return WebsocketMessageType.UNICAST;
			}
			throw new IllegalArgumentException(type.toString());
		}
	}
	
	@WebSocket(maxTextMessageSize = 64 * 1024)
	public class JettyWebSocket {

		private Session session;

		@OnWebSocketClose
		public void onClose(int statusCode, String reason) {
			this.session = null;
		}

		@OnWebSocketConnect
		public void onConnect(Session session) {
			try {
				// send REGISTER message
				this.session = session;
				WebsocketMessage registerMessage = new WebsocketMessage(WebsocketMessageType.REGISTER, serviceId.toString(),
						null, null, null);
				String registerJson = gson.toJson(registerMessage);
				send(registerJson);
			} catch (Throwable t) {
				exceptionHandler.error("Registration Failure", t);
			}
		}

		@OnWebSocketMessage
		public void onMessage(String json) {
			WebsocketMessage m = gson.fromJson(json, WebsocketMessage.class);
			switch (m.getType()) {
			case ANNOUNCE:
				announceTransport.handleAnnouncement(RID.create(m.getFrom()));
				break;
			case BROADCAST:
				bcastTransport.handleBcastMessage(RID.create(m.getFrom()), RID.create(m.getGroup()), GrowableBuffer.fromBase64(m.getData()));
				break;
			case JOIN:
				bcastTransport.handleBcastJoin(RID.create(m.getFrom()), RID.create(m.getGroup()));
				break;
			case LEAVE:
				bcastTransport.handleBcastLeave(RID.create(m.getFrom()), RID.create(m.getGroup()));
				break;
			case PROBE:
				announceTransport.incognito_announce(serviceId);
				break;
			case UNICAST:
				ucastTransport.handleUcastMessage(RID.create(m.getFrom()), RID.create(m.getTo()), GrowableBuffer.fromBase64(m.getData()));
				break;
			default:
				logger.debug("Unrecognized type: " + m.getType());
				break;
			}
		}

		public synchronized void send(String message) throws IOException {
			Session s = session;
			if (s != null) {
				s.getRemote().sendString(message);
			}
		}
	}

}
