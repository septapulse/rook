package io.septapulse.rook.api.transport.simple;

/**
 * Used by the
 * {@link Publisher#publish(MessageType, rook.api.RID, rook.api.RID, rook.api.RID, rook.api.transport.GrowableBuffer)}
 * message to discern the type of event being sent.
 * 
 * @author Eric Thill
 *
 */
public enum MessageType {
	UCAST_MESSAGE(0), BCAST_MESSAGE(1), BCAST_JOIN(2), BCAST_LEAVE(3), ANNOUNCE(4), PROBE(5);
	private final byte value;
	private MessageType(int value) {
		this.value = (byte)value;
	}
	public byte getValue() {
		return value;
	}
	public static MessageType fromValue(byte value) {
		switch(value) {
		case 0:
			return UCAST_MESSAGE;
		case 1:
			return BCAST_MESSAGE;
		case 2:
			return BCAST_JOIN;
		case 3:
			return BCAST_LEAVE;
		case 4:
			return ANNOUNCE;
		case 5:
			return PROBE;
		default:
			return null;
		}
	}
}
