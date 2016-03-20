package rook.api.transport.event;

import rook.api.RID;

/**
 * A broadcast message is sent once by one service and is consumed by any other
 * service who are joined to the broadcast group.
 * 
 * @author Eric Thill
 *
 * @param <T>
 */
public class BroadcastMessage<T> {
	private final RID from = new RID();
	private final RID group = new RID();
	private T payload;

	public void setPayload(T payload) {
		this.payload = payload;
	}

	/**
	 * Get the service ID that this message was addressed from
	 * 
	 * @return The service ID that this message was addressed from
	 */
	public RID getFrom() {
		return from;
	}

	/**
	 * Get the group the message is being sent on
	 * 
	 * @return The group the message is being sent on
	 */
	public RID getGroup() {
		return group;
	}

	/**
	 * Get the message payload being sent
	 * 
	 * @return The message payload being sent
	 */
	public T getPayload() {
		return payload;
	}

	@Override
	public String toString() {
		return "BroadcastMessage [from=" + from + ", group=" + group + ", payload=" + payload + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((payload == null) ? 0 : payload.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
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
		BroadcastMessage<?> other = (BroadcastMessage<?>) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (payload == null) {
			if (other.payload != null)
				return false;
		} else if (!payload.equals(other.payload))
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		return true;
	}

}
