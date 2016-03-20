package rook.api.transport.event;

import rook.api.RID;

/**
 * An event that indicates a service has left a broadcast group. Services can use this
 * to determine when they should stop sending broadcast messages rather than
 * always send them blindly.
 * 
 * @author Eric Thill
 *
 */
public class BroadcastLeave {
	private RID from;
	private RID group;
	
	public void setFrom(RID from) {
		this.from = from;
	}
	
	public void setGroup(RID group) {
		this.group = group;
	}
	
	/**
	 * Get the leaving service ID
	 * 
	 * @return the leaving service ID
	 */
	public RID getFrom() {
		return from;
	}
	
	/**
	 * Get the group the service is leaving
	 * 
	 * @return the group the service is leaving
	 */
	public RID getGroup() {
		return group;
	}

	@Override
	public String toString() {
		return "BroadcastLeave [from=" + from + ", group=" + group + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BroadcastLeave other = (BroadcastLeave) obj;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		return true;
	}
	
}
