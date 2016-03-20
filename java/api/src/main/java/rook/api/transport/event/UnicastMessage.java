package rook.api.transport.event;

import rook.api.RID;

/**
 * A unicast message is sent by one service directly to another service.
 * 
 * @author Eric Thill
 *
 * @param <T>
 */
public class UnicastMessage<T> {
	private final RID from = new RID();
	private final RID to = new RID();
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
	 * Get the service ID that this message was addressed to
	 * 
	 * @return The service ID that this message was addressed to
	 */
	public RID getTo() {
		return to;
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
		return "UnicastMessage [from=" + from + ", to=" + to + ", payload=" + payload + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((payload == null) ? 0 : payload.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		UnicastMessage<?> other = (UnicastMessage<?>) obj;
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
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}

}
