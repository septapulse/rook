package rook.api.transport.simple;

import rook.api.RID;
import rook.api.Router;
import rook.api.transport.GrowableBuffer;

/**
 * Used by {@link SimpleAnnounceTransport}, {@link SimpleBroadcastTransport},
 * and {@link SimpleUnicastTransport} to provide the mechanism to publish to any
 * {@link Router} implementation.
 * 
 * @author Eric Thill
 *
 */
public interface Publisher {
	void publish(MessageType type, RID from, RID to, RID group, GrowableBuffer msg);
}
