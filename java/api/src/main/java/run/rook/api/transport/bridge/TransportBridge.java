package run.rook.api.transport.bridge;

import static run.rook.api.config.Arg.arg;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import run.rook.api.RID;
import run.rook.api.ServiceLauncher;
import run.rook.api.config.Args;
import run.rook.api.reflect.Instantiate;
import run.rook.api.transport.ControllableTransport;
import run.rook.api.transport.GrowableBuffer;
import run.rook.api.transport.Transport;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
public class TransportBridge {

	private static final Logger LOGGER = LoggerFactory.getLogger(TransportBridge.class);
	
	public static void main(String... argsArr) {
		try {
			Args args = Args.parse(argsArr,
					arg("t1", "transport1-type", true, true, false, "Fully Qualified Transport 1 Class"),
					arg("c1", "transport1-config", true, true, false, "Transport 1 Configuration"),
					arg("t2", "transport2-type", true, true, false, "Fully Qualified Transport 2 Class"),
					arg("c2", "transport2-config", true, true, false, "Transport 2 Configuration"));
			if(args == null)
				return;
			Class<?> transport1Type = Class.forName(args.getValue("t1"));
			String transport1Config = args.getValue("c1");
			Class<?> transport2Type = Class.forName(args.getValue("t2"));
			String transport2Config = args.getValue("c2");
			LOGGER.info(transport1Type.getSimpleName() + " <-> " + transport2Type.getSimpleName());
			ControllableTransport t1 = Instantiate.instantiate(transport1Type, transport1Config);
			t1.setServiceId(RID.create(0));
			t1.setExceptionHandler(TransportBridge::logError);
			t1.setRespondToProbes(false);
			t1.start();
			ControllableTransport t2 = Instantiate.instantiate(transport2Type, transport2Config);
			t2.setServiceId(RID.create(0));
			t2.setExceptionHandler(TransportBridge::logError);
			t2.setRespondToProbes(false);
			t2.start();
			new TransportBridge(t1, t2).start();
		} catch(Throwable t) {
			LoggerFactory.getLogger(ServiceLauncher.class).error("Could not launch Service", t);
			System.exit(-1);
		}
	}
	
	private static void logError(String error, Throwable t) {
		if(t == null)
			LOGGER.error(error+"");
		else
			LOGGER.error(error+"", t);
	}
	
	private final Transport t1;
	private final Transport t2;
	
	public TransportBridge(Transport t1, Transport t2) {
		this.t1 = t1;
		this.t2 = t2;
	}
	
	// FIXME NEED TO PERIODICALLY REFRESH THIS INFO
	private final Set<RID> t1Services = Collections.synchronizedSet(new HashSet<>());
	private final Set<RID> t2Services = Collections.synchronizedSet(new HashSet<>());
	// key=group, value=subscribed
	private final Map<RID, Set<RID>> t1Joins = Collections.synchronizedMap(new HashMap<>());
	private final Map<RID, Set<RID>> t2Joins = Collections.synchronizedMap(new HashMap<>());
	
	public void start() {
		LOGGER.info("Starting");
		t1.announce().addAnnouncementConsumer(this::handleT1Announcement);
		t2.announce().addAnnouncementConsumer(this::handleT2Announcement);
		t1.announce().addProbeConsumer(this::handleT1Probe);
		t2.announce().addProbeConsumer(this::handleT2Probe);
		t1.bcast().addJoinConsumer(this::handleT1Join);
		t2.bcast().addJoinConsumer(this::handleT2Join);
		t1.bcast().addLeaveConsumer(this::handleT1Leave);
		t2.bcast().addLeaveConsumer(this::handleT2Leave);
		t1.bcast().incognito_addMessageConsumer(this::handleT1BCast);
		t2.bcast().incognito_addMessageConsumer(this::handleT2BCast);
		t1.ucast().incognito_addMessageConsumer(this::handleT1UCast);
		t2.ucast().incognito_addMessageConsumer(this::handleT2UCast);

		// probe for announce and bcast join info
		LOGGER.info("Probing " + t1.getClass().getSimpleName());
		t1.announce().probe();
		LOGGER.info("Probing " + t2.getClass().getSimpleName());
		t2.announce().probe();
		
		LOGGER.info("Started");
	}
	
	private void handleT1Announcement(RID serviceId) {
		handleAnnouncement(serviceId, t2, t2Services, t1Services);
	}
	
	private void handleT2Announcement(RID serviceId) {
		handleAnnouncement(serviceId, t1, t1Services, t2Services);
	}
	
	private static void handleAnnouncement(RID serviceId, Transport t, Set<RID> otherServices, Set<RID> myServices) {
		if(!otherServices.contains(serviceId)) {
			if(myServices.add(serviceId.immutable())) {
				LOGGER.info("Discovered Service: " + serviceId);
			}
			t.announce().incognito_announce(serviceId);
		}
	}
	
	private void handleT1Probe(RID from) {
		handlProbe(from, t2, t1Services);
	}
	
	private void handleT2Probe(RID from) {
		handlProbe(from, t1, t2Services);
	}
	
	private static void handlProbe(RID from, Transport t, Set<RID> allowedFromServices) {
		if(allowedFromServices.contains(from)) {
			t.announce().probe();
		}
	}
	
	private void handleT1Join(RID from, RID group) {
		handleJoin(from, group, t2, t1Services, t1Joins);
	}
	
	private void handleT2Join(RID from, RID group) {
		handleJoin(from, group, t1, t2Services, t2Joins);
	}
	
	private static void handleJoin(RID from, RID group, Transport t, Set<RID> allowedFromServices, Map<RID, Set<RID>> joinsToUpdate) {
		if(allowedFromServices.contains(from)) {
			synchronized (joinsToUpdate) {
				Set<RID> services = joinsToUpdate.get(group);
				if(services == null) {
					services = new HashSet<>();
					joinsToUpdate.put(group.immutable(), services);
				}
				services.add(from.immutable());
			}
			t.bcast().incognito_join(from, group);
		}
	}
	
	private void handleT1Leave(RID from, RID group) {
		handleLeave(from, group, t2, t1Services, t1Joins);
	}
	
	private void handleT2Leave(RID from, RID group) {
		handleLeave(from, group, t1, t2Services, t2Joins);
	}
	
	private static void handleLeave(RID from, RID group, Transport t, Set<RID> allowedFromServices, Map<RID, Set<RID>> joinsToUpdate) {
		if(allowedFromServices.contains(from)) {
			synchronized (joinsToUpdate) {
				Set<RID> services = joinsToUpdate.get(group);
				if(services != null) {
					services.remove(from);
					if(services.size() == 0) {
						joinsToUpdate.remove(group);
					}
				}
			}
			t.bcast().incognito_leave(from, group);
		}
	}

	private void handleT1BCast(RID from, RID group, GrowableBuffer buf) {
		handleBCast(from, group, buf, t2, t1Services, t2Joins);
	}
	
	private void handleT2BCast(RID from, RID group, GrowableBuffer buf) {
		handleBCast(from, group, buf, t1, t2Services, t1Joins);
	}
	
	private static void handleBCast(RID from, RID group, GrowableBuffer buf, Transport t, 
			Set<RID> allowedFromServices, Map<RID, Set<RID>> allowedGroups) {
		// only send if it is from a service announced on this side, and if the group was joined from the other side
		if(allowedFromServices.contains(from) && allowedGroups.containsKey(group)) {
			t.bcast().incognito_send(from, group, buf);
		}
	}

	private void handleT1UCast(RID from, RID to, GrowableBuffer payload) {
		handleUCast(from, to, payload, t2, t2Services);
	}
	
	private void handleT2UCast(RID from, RID to, GrowableBuffer payload) {
		handleUCast(from, to, payload, t1, t1Services);
	}
	
	private static void handleUCast(RID from, RID to, GrowableBuffer payload, Transport t, Set<RID> allowedToServices) {
		// only send if the "to" service was announced from the other side
		if(allowedToServices.contains(to)) {
			t.ucast().incognito_send(from, to, payload);
		}
	}
	
}
