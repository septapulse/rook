package rook.api.transport.simple;

/**
 * Used by the
 * {@link Publisher#publish(MessageType, rook.api.RID, rook.api.RID, rook.api.RID, rook.api.transport.GrowableBuffer)}
 * message to discern the type of event being sent.
 * 
 * @author Eric Thill
 *
 */
public enum MessageType {
	UCAST_MESSAGE, BCAST_MESSAGE, BCAST_JOIN, BCAST_LEAVE, ANNOUNCE, PROBE;
}
