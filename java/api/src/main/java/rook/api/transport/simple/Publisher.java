package rook.api.transport.simple;

import rook.api.RID;
import rook.api.transport.GrowableBuffer;

/**
 * Used by {@link SimpleAnnounceTransport}, {@link SimpleBroadcastTransport},
 * and {@link SimpleUnicastTransport} to a provide message publishing mechanism.
 * 
 * @author Eric Thill
 *
 */
public interface Publisher {
	void publish(MessageType type, RID from, RID to, RID group, GrowableBuffer msg);
}
