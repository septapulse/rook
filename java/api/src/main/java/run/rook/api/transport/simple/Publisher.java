package run.rook.api.transport.simple;

import run.rook.api.RID;
import run.rook.api.transport.GrowableBuffer;

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
