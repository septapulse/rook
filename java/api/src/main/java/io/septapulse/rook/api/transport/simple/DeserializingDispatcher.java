package io.septapulse.rook.api.transport.simple;

import org.agrona.DirectBuffer;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.transport.GrowableBuffer;

/**
 * Utility to be used in conjunction with a {@link SerializingPublisher} to
 * parse the binary data and dispatch to the given Announce/Broadcast/Unicast
 * handlers.
 * 
 * @author Eric Thill
 *
 */
public class DeserializingDispatcher {

	private final RID from = new RID();
	private final RID to = new RID();
	private final RID group = new RID();
	private final GrowableBuffer msg = GrowableBuffer.allocate(1024); // FIXME
																		// configurable
	private final SimpleAnnounceTransport announce;
	private final SimpleBroadcastTransport bcast;
	private final SimpleUnicastTransport ucast;

	public DeserializingDispatcher(SimpleAnnounceTransport announce, SimpleBroadcastTransport bcast,
			SimpleUnicastTransport ucast) {
		this.announce = announce;
		this.bcast = bcast;
		this.ucast = ucast;
	}

	public void dispatch(DirectBuffer buffer, int offset, int length) {
		MessageType type = MessageType.fromValue(buffer.getByte(offset));
		int msgLen;

		switch (type) {
		case PROBE:
			from.setValue(buffer.getLong(offset + 1));
			announce.handleProbe(from);
			bcast.handleProbe();
			break;
		case ANNOUNCE:
			from.setValue(buffer.getLong(offset + 1));
			announce.handleAnnouncement(from);
			break;
		case BCAST_JOIN:
			from.setValue(buffer.getLong(offset + 1));
			group.setValue(buffer.getLong(offset + 9));
			bcast.handleBcastJoin(from, group);
			break;
		case BCAST_LEAVE:
			from.setValue(buffer.getLong(offset + 1));
			group.setValue(buffer.getLong(offset + 9));
			bcast.handleBcastLeave(from, group);
			break;
		case BCAST_MESSAGE:
			from.setValue(buffer.getLong(offset + 1));
			group.setValue(buffer.getLong(offset + 9));
			msgLen = length - 17;
			msg.reserve(msgLen, false);
			buffer.getBytes(offset + 17, msg.bytes(), 0, msgLen);
			msg.length(msgLen);
			bcast.handleBcastMessage(from, group, msg);
			break;
		case UCAST_MESSAGE:
			from.setValue(buffer.getLong(offset + 1));
			to.setValue(buffer.getLong(offset + 9));
			msgLen = length - 17;
			msg.reserve(msgLen, false);
			buffer.getBytes(offset + 17, msg.bytes(), 0, msgLen);
			msg.length(msgLen);
			ucast.handleUcastMessage(from, to, msg);
			break;
		}
	}
}
