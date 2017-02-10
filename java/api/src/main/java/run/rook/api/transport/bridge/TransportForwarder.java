package run.rook.api.transport.bridge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import run.rook.api.RID;
import run.rook.api.transport.GrowableBuffer;
import run.rook.api.transport.Transport;

class TransportForwarder {
	// FIXME NEED TO PERIODICALLY REFRESH THIS INFO
	private final Set<RID> fromServices = new HashSet<>();
	private final Set<RID> toServices = new HashSet<>();
	private final Map<RID, Set<RID>> toBCastGroups = new HashMap<>();
	private final Transport fromTransport;
	private final Transport toTransport;
	
	public TransportForwarder(Transport from, Transport to) {
		this.fromTransport = from;
		this.toTransport = to;
	}
	
	public void start() {
		fromTransport.announce().addAnnouncementConsumer(this::handleFromAnnouncement);
		toTransport.announce().addAnnouncementConsumer(this::handleToAnnouncement);
		toTransport.bcast().addJoinConsumer(this::handleToJoin);
		toTransport.bcast().addLeaveConsumer(this::handleToLeave);
		
		fromTransport.announce().addProbeConsumer(this::handleFromProbe);
		fromTransport.bcast().addJoinConsumer(this::handleFromJoin);
		fromTransport.bcast().addLeaveConsumer(this::handleFromLeave);
		fromTransport.bcast().incognito_addMessageConsumer(this::handleFromBCast);
		fromTransport.ucast().incognito_addMessageConsumer(this::handleFromUCast);
		
		// probe for announce and bcast join info
		toTransport.announce().probe();
		fromTransport.announce().probe();
	}
	
	private void handleToAnnouncement(RID serviceId) {
		toServices.add(serviceId.immutable());
	}
	
	private void handleFromAnnouncement(RID serviceId) {
		if(!toServices.contains(serviceId)) {
			System.out.println("handleFromAnnouncement " + serviceId);
			fromServices.add(serviceId.immutable());
//			toTransport.announce().incognito_announce(serviceId);
		}
	}
	
	private void handleToJoin(RID from, RID group) {
		if(toBCastGroups.containsKey(from)) {
			Set<RID> grps = toBCastGroups.get(from);
			if(grps == null) {
				grps = new HashSet<>();
				toBCastGroups.put(from, grps);
			}
			grps.add(group);
		}
	}
	
	private void handleToLeave(RID from, RID group) {
		if(toBCastGroups.containsKey(from)) {
			Set<RID> grps = toBCastGroups.get(from);
			if(grps != null) {
				grps.remove(group);
				if(grps.size() == 0) {
					toBCastGroups.remove(from);
				}
			}
		}
	}
	
	private void handleFromProbe(RID from) {
		if(fromServices.contains(from)) {
			toTransport.announce().probe();
		}
	}
	
	private void handleFromJoin(RID from, RID group) {
		toTransport.bcast().incognito_join(from, group);
	}
	
	private void handleFromLeave(RID from, RID group) {
		toTransport.bcast().incognito_leave(from, group);
	}
	
	private void handleFromBCast(RID from, RID group, GrowableBuffer buf) {
		toTransport.bcast().incognito_send(from, group, buf);
	}
	
	private void handleFromUCast(RID from, RID to, GrowableBuffer payload) {
		toTransport.ucast().incognito_send(from, to, payload);
	}
}
