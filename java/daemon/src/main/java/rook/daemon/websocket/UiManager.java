package rook.daemon.websocket;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Collection;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import rook.cli.message.Result;
import rook.cli.message.ui.UiInfo;
import rook.cli.message.ui.UiMessageType;
import rook.cli.message.ui.UiRequest;
import rook.cli.message.ui.UiResponse;

@WebSocket
public class UiManager {
	
	public static final String UI_MANAGER_PROTOCOL = "UI";

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Gson gson = new Gson();
	private final ZipManager<UiInfo> zipManager;
	
	public UiManager(File platformDir, File usrDir) {
		platformDir.mkdirs();
		usrDir.mkdirs();
		zipManager = new ZipManager<>(UiInfo.class, 
				this::getId, this::setId, this::getName, 
				new File[] {platformDir, usrDir}, usrDir);
	}
	
	private String getId(UiInfo o) {
		return o.getId();
	}
	
	private void setId(UiInfo o, String id) {
		o.setId(id);
	}
	
	private String getName(UiInfo o) {
		return o.getName();
	}
	
	public void init() {
		zipManager.refresh();
	}
	
	@OnWebSocketMessage
	public void onText(Session session, String message) throws IOException {
		UiRequest req = gson.fromJson(message, UiRequest.class);
		Result result;
		try {
			switch (req.getType()) {
			case LIST:
				Collection<UiInfo> packages = zipManager.all();
				result = new Result().setSuccess(packages != null);
				send(session, new UiResponse()
						.setType(UiMessageType.LIST)
						.setResult(result)
						.setUIs(packages));
				break;
			case GET:
				UiInfo pkg = zipManager.get(req.getId());
				result = new Result().setSuccess(pkg != null);
				if(pkg == null) {
					result.setError("Package '" + req.getId() + "' does not exist");
				}
				send(session, new UiResponse()
						.setType(UiMessageType.LIST)
						.setResult(result)
						.setUI(pkg));
				break;
			case ADD:
				zipManager.add(Base64.getDecoder().decode(req.getData()));
				result = new Result().setSuccess(true);
				send(session, new UiResponse()
						.setType(UiMessageType.ADD)
						.setResult(result));
				break;
			case REMOVE:
				boolean success = zipManager.remove(req.getId());
				result = new Result().setSuccess(success);
				if(!success) {
					result.setError("No package with id=" + req.getId());
				}
				send(session, new UiResponse()
						.setType(UiMessageType.REMOVE)
						.setResult(result));
				break;
			case REFRESH:
				zipManager.refresh();
				result = new Result().setSuccess(true);
				send(session, new UiResponse()
						.setType(UiMessageType.REFRESH)
						.setResult(result));
				break;
				
			}
		} catch(Throwable t) {
			logger.error("PackageManager failed to process " + message);
			result = new Result().setSuccess(false).setError(t);
			send(session, new UiResponse()
					.setType(UiMessageType.REFRESH)
					.setResult(result));
		}
	}

	private void send(Session session, UiResponse m) {
		try {
			session.getRemote().sendString(gson.toJson(m));
		} catch (IOException e) {
			if(logger.isDebugEnabled()) {
				logger.debug("Could not send response", e);
			}
		}
	}
	
	public ZipManager<UiInfo> getZipManager() {
		return zipManager;
	}
	
}
