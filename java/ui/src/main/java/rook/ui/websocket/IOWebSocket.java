package rook.ui.websocket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import rook.api.RID;
import rook.core.io.proxy.message.Cap;
import rook.core.io.proxy.message.CapType;
import rook.core.io.proxy.message.IOValue;
import rook.ui.environment.Environment;
import rook.ui.websocket.message.IORequest;
import rook.ui.websocket.message.IOResponse;

/**
 * WebSocket that handles IO messages (inputs, outputs, caps)
 * 
 * @author Eric Thill
 *
 */
@Deprecated
@WebSocket
public class IOWebSocket {

	private static final String TYPE_INPUTS = "inputs";
	private static final String TYPE_OUTPUTS = "outputs";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Set<Session> streamSessions = Collections.synchronizedSet(new LinkedHashSet<>());
	private final Gson gson = new Gson();
	private final Environment environment;
	
	public IOWebSocket(Environment environment) {
		this.environment = environment;
		environment.getIoProxy().inputs().addBatchConsumer(this::processInput);
		environment.getIoProxy().outputs().addBatchConsumer(this::processOutput);
		environment.getIoProxy().caps().addConsumer(this::processCaps);
	}
	
	@OnWebSocketMessage
	public void onText(Session session, String message) throws IOException {
		IORequest req = gson.fromJson(message, IORequest.class);
		switch (req.getType()) {
		case "stream":
			// may have some old lingering state to clear
			environment.getIoProxy().reset();
			List<Cap> caps = environment.getIoProxy().caps().getCaps();
			IOResponse respInputs = createInputMessage(caps);
			IOResponse respOutputs = createOutputMessage(caps);
			session.getRemote().sendString(gson.toJson(respInputs));
			session.getRemote().sendString(gson.toJson(respOutputs));
			streamSessions.add(session);
			break;
		}
	}
	
	private IOResponse createInputMessage(List<Cap> caps) {
		IOResponse respInputs = new IOResponse();
		respInputs.setType(TYPE_INPUTS);
		for (Cap c : caps) {
			if (c.getCapType() == CapType.INPUT) {
				IOValue iov = environment.getIoProxy().inputs().getValue(c.getId());
				String v = iov == null ? null : iov.getValueAsString();
				respInputs.addValue(new IOResponse.Value().setId(c.getId().toString()).setValue(v));
			}
		}
		return respInputs;
	}
	
	private IOResponse createOutputMessage(List<Cap> caps) {
		IOResponse respOutputs = new IOResponse();
		respOutputs.setType(TYPE_OUTPUTS);
		for (Cap c : caps) {
			if (c.getCapType() == CapType.OUTPUT) {
				IOValue iov = environment.getIoProxy().outputs().getValue(c.getId());
				String v = iov == null ? null : iov.getValueAsString();
				respOutputs.addValue(new IOResponse.Value().setId(c.getId().toString()).setValue(v));
			}
		}
		return respOutputs;
	}

	private void processCaps(List<Cap> caps) {
		IOResponse respInputs = createInputMessage(caps);
		dispatch(gson.toJson(respInputs));
		IOResponse respOutputs = createOutputMessage(caps);
		dispatch(gson.toJson(respOutputs));
	}

	private List<IdValuePair> inputs = new ArrayList<>();
	private synchronized void processInput(RID id, IOValue value, boolean endOfBatch) {
		// FIXME reuse inputs
		inputs.add(new IdValuePair().set(id, value));
		if(endOfBatch) {
			dispatch(TYPE_INPUTS, inputs);
			// FIXME reuse outputs
			inputs.clear();
		}
	}

	private List<IdValuePair> outputs = new ArrayList<>();
	private synchronized void processOutput(RID id, IOValue value, boolean endOfBatch) {
		outputs.add(new IdValuePair().set(id, value));
		if(endOfBatch) {
			dispatch(TYPE_OUTPUTS, outputs);
			// FIXME reuse outputs
			outputs.clear();
		}
	}
	
	private void dispatch(String type, List<IdValuePair> values) {
		IOResponse resp = new IOResponse();
		resp.setType(type);
		for(IdValuePair v : values) {
			resp.addValue(new IOResponse.Value().setId(v.id.toString()).setValue(v.val.getValueAsString()));
		}
		String msg = gson.toJson(resp);
		dispatch(msg);
	}
	
	private void dispatch(String msg) {
		synchronized (streamSessions) {
			Iterator<Session> t = streamSessions.iterator();
			while(t.hasNext()) {
				Session s = t.next();
				if(s.isOpen()) {
					try {
						s.getRemote().sendString(msg);
					} catch(Throwable error) {
						logger.info("Logger client closed: " + s.getRemoteAddress());
						t.remove();
					}
				} else {
					t.remove();
				}
			}
		}
	}
	
	static class IdValuePair {
		public final RID id = new RID();
		public final IOValue val = new IOValue();
		public IdValuePair set(RID id, IOValue val) {
			this.id.copyFrom(id);
			this.val.copyFrom(val);
			return this;
		}
	}
}
