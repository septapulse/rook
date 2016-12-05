package io.septapulse.rook.core.transport.websocket;

import java.util.LinkedHashSet;
import java.util.Set;

class SessionInfo {
	private String id;
	private Set<String> groups;
	private boolean incognito;
	
	public String getId() {
		return id;
	}
	
	public SessionInfo setId(String id) {
		this.id = id;
		return this;
	}
	
	public boolean containsGroup(String group) {
		return groups != null && groups.contains(group);
	}
	
	public SessionInfo addGroup(String group) {
		if(groups == null) {
			groups = new LinkedHashSet<>();
		}
		groups.add(group);
		return this;
	}
	
	public SessionInfo removeGroup(String group) {
		if(groups != null) {
			groups.remove(group);
			if(groups.size() == 0) {
				groups = null;
			}
		}
		return this;
	}
	
	public boolean isIncognito() {
		return incognito;
	}
	
	public SessionInfo setIncognito(boolean incognito) {
		this.incognito = incognito;
		return this;
	}

	@Override
	public String toString() {
		return "SessionInfo [id=" + id + " groups=" + groups + ", incognito=" + incognito + "]";
	}
	
}
