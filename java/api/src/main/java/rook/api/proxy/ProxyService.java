package rook.api.proxy;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rook.api.InitException;
import rook.api.RID;
import rook.api.Service;
import rook.api.transport.GrowableBuffer;
import rook.api.transport.Transport;
import rook.api.transport.event.BroadcastJoin;
import rook.api.transport.event.BroadcastLeave;
import rook.api.transport.event.BroadcastMessage;
import rook.api.transport.event.UnicastMessage;
import rook.api.util.BufferUtil;

/**
 * A {@link Service} that is able to connect multiple Rook environments together 
 * to create a single uniform environment. This allows for distributed computing
 * and for applications written in multiple languages to communicate. This is an
 * abstract implementation that takes care of messaging parsing, handling, and 
 * serialization. The abstract methods that are implemented by the concrete class
 * take care of sending and receiving encoded messages on the wire.   
 * 
 * @author Eric Thill
 *
 */
public abstract class ProxyService implements Service {

	private static final long STARTUP_TIMEOUT = 2000;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Set<RID> internalServices = Collections.synchronizedSet(new LinkedHashSet<>());
	private final Set<RID> externalServices = Collections.synchronizedSet(new LinkedHashSet<>());
	private final Map<RID, Set<RID>> internalJoins = Collections.synchronizedMap(new LinkedHashMap<>()); // key=group, value=services
	private final Map<RID, Set<RID>> externalJoins = Collections.synchronizedMap(new LinkedHashMap<>()); // key=group, value=services

	private final GrowableBuffer reusedInternalSendMessage;
	private Transport transport;
	private ProxySender sender;
	
	public ProxyService(int payloadSize) {
		reusedInternalSendMessage = GrowableBuffer.allocate(payloadSize);
	}

	@Override
	public void setTransport(Transport transport) {
		this.transport = transport;
	}

	@Override
	public final void init() throws InitException {
		logger.info("Initializing " + getClass().getName());
		try {
			sender = initialize();
			transport.announce().addAnnouncementConsumer(announcementConsumer);
			transport.ucast().incognito_addMessageConsumer(ucastConsumer);
			transport.bcast().incognito_addMessageConsumer(bcastConsumer);
			transport.bcast().addJoinConsumer(joinConsumer);
			transport.bcast().addLeaveConsumer(leaveConsumer);
			// will trigger this service and every other ProxySender to send current state
			logger.info("Sending Resend Request");
		} catch(Throwable t) {
			throw new InitException("Could not initialize " + getClass().getName(), t);
		}
		
		sender.sendResendRequest();
		logger.info("Waiting " + STARTUP_TIMEOUT + " milliseconds for Resend Responses");
		try {
			Thread.sleep(STARTUP_TIMEOUT);
		} catch (InterruptedException e) {

		}
		
		logger.info("Initialized " + getClass().getName());
	}
	
	public abstract ProxySender initialize() throws InitException;
	
	private final Consumer<RID> announcementConsumer = new Consumer<RID>() {
		@Override
		public void accept(RID id) {
			if(!externalServices.contains(id)) {
				logger.info("Local Service Announcement: " + id);
				internalServices.add(id.unmodifiable());
				sender.sendAnnouncement(id);
			}
		}
	};
	
	private final Consumer<UnicastMessage<GrowableBuffer>> ucastConsumer = new Consumer<UnicastMessage<GrowableBuffer>>() {
		@Override
		public void accept(UnicastMessage<GrowableBuffer> t) {
			if(internalServices.contains(t.getFrom())) {
				sender.sendUnicastMessage(t.getFrom(), t.getTo(), t.getPayload());
			}
		}
	};
	
	private final Consumer<BroadcastJoin> joinConsumer  = new Consumer<BroadcastJoin>() {
		@Override
		public void accept(BroadcastJoin t) {
			if(internalServices.contains(t.getFrom())) {
				addJoin(internalJoins, t.getFrom(), t.getGroup());
				sender.sendJoin(t.getFrom(), t.getGroup());
			}
		}
	};
	
	private static void addJoin(Map<RID, Set<RID>> map, RID from, RID group) {
		Set<RID> froms = map.get(group);
		if(froms == null) {
			froms = new HashSet<>();
			map.put(group.unmodifiable(), froms);
		}
		froms.add(from.unmodifiable());
	}
	
	private final Consumer<BroadcastLeave> leaveConsumer  = new Consumer<BroadcastLeave>() {
		@Override
		public void accept(BroadcastLeave t) {
			if(internalServices.contains(t.getFrom())) {
				removeJoin(internalJoins, t.getFrom(), t.getGroup());
				sender.sendLeave(t.getFrom(), t.getGroup());
			}
		}
	};
	
	private static void removeJoin(Map<RID, Set<RID>> map, RID from, RID group) {
		Set<RID> froms = map.get(group);
		if(froms != null) {
			froms.remove(from);
			if(froms.size() == 0) {
				map.remove(group);
			}
		}
	}
	
	private final Consumer<BroadcastMessage<GrowableBuffer>> bcastConsumer = new Consumer<BroadcastMessage<GrowableBuffer>>() {
		@Override
		public void accept(BroadcastMessage<GrowableBuffer> t) {
			// TODO inStartup is a hack because messages can come before the PROBE response is complete, 
			// so we forward everything for a few seconds at startup for now
			if(internalServices.contains(t.getFrom()) && externalJoins.containsKey(t.getGroup())) {
				sender.sendBroadcastMessage(t.getFrom(), t.getGroup(), t.getPayload());
			}
		}
	};
	
	/**
	 * To be called by the implementing class when a message has been received
	 * 
	 * @param payload
	 * @param offset
	 * @param length
	 */
	protected void handlePayload(byte[] payload, int offset, int length) {
		if(logger.isDebugEnabled()) {
			logger.debug(transport + " Handling @" + offset + ": " + Arrays.toString(Arrays.copyOf(payload, offset+length)));
		}
		byte type = payload[offset];
		switch(type) {
		case ProxyConstants.TYPE_ANNOUNCEMENT:
			handleAnnouncement(payload, offset+1, length-1);
			break;
		case ProxyConstants.TYPE_BROADCAST_JOIN:
			handleBroadcastJoin(payload, offset+1, length-1);
			break;
		case ProxyConstants.TYPE_BROADCAST_LEAVE:
			handleBroadcastLeave(payload, offset+1, length-1);
			break;
		case ProxyConstants.TYPE_BROADCAST_MESSAGE:
			handleBroadcastMessage(payload, offset+1, length-1);
			break;
		case ProxyConstants.TYPE_UNICAST_MESSAGE:
			handleUnicastMessage(payload, offset+1, length-1);
			break;
		case ProxyConstants.TYPE_RESEND_REQUEST:
			// new thread: some bus implementations cannot send from the callback thread
			new Thread(this::handleResendRequest, "Resend Request Handler").start();
			break;
		}
	}
	
	private void handleAnnouncement(byte[] payload, int offset, int length) {
		RID id = RID.create(BufferUtil.readLong(payload, offset));
		if(!internalServices.contains(id)) { // ignore loopback
			logger.info("External Service Announcement: " + id);
			externalServices.add(id);
			// FIXME !started ?
			transport.announce().incognito_announce(id);
		}
	}

	private void handleBroadcastJoin(byte[] payload, int offset, int length) {
		RID from = RID.create(BufferUtil.readLong(payload, offset));
		if(!internalServices.contains(from)) { // ignore loopback
			RID group = RID.create(BufferUtil.readLong(payload, offset+8));
			addJoin(externalJoins, from, group);
			transport.bcast().incognito_join(from, group);
		}
	}

	private void handleBroadcastLeave(byte[] payload, int offset, int length) {
		RID from = RID.create(BufferUtil.readLong(payload, offset));
		if(!internalServices.contains(from)) { // ignore loopback
			RID group = RID.create(BufferUtil.readLong(payload, offset+8));
			removeJoin(externalJoins, from, group);
			transport.bcast().incognito_join(from, group);
		}
	}

	private void handleBroadcastMessage(byte[] payload, int offset, int length) {
		RID from = RID.create(BufferUtil.readLong(payload, offset));
		if(!internalServices.contains(from)) { // ignore loopback
			RID group = RID.create(BufferUtil.readLong(payload, offset+8));
			if(internalJoins.containsKey(group)) {
				reusedInternalSendMessage.reset(false);
				reusedInternalSendMessage.put(payload, offset+16, length-16);
				transport.bcast().incognito_send(from, group, reusedInternalSendMessage);
			}
		}
	}

	private void handleUnicastMessage(byte[] payload, int offset, int length) {
		RID from = RID.create(BufferUtil.readLong(payload, offset));
		if(!internalServices.contains(from)) { // ignore loopback
			RID to = RID.create(BufferUtil.readLong(payload, offset+8));
			if(internalServices.contains(to)) {
				reusedInternalSendMessage.reset(false);
				reusedInternalSendMessage.put(payload, offset+16, length-16);
				transport.ucast().incognito_send(from, to, reusedInternalSendMessage);
			}
		}
	}

	protected void handleResendRequest() {
		synchronized (internalServices) {
			for(RID id : internalServices) {
				sender.sendAnnouncement(id);
			}
		}
		synchronized (internalJoins) {
			for(Map.Entry<RID, Set<RID>> e : internalJoins.entrySet()) {
				RID group = e.getKey();
				for(RID service : e.getValue()) {
					sender.sendJoin(service, group);
				}
			}
		}
	}
	
}
