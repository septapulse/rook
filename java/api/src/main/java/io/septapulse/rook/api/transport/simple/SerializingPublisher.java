package io.septapulse.rook.api.transport.simple;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.transport.GrowableBuffer;

/**
 * Abstract {@link Publisher} implementation that serializes the various message
 * inputs to binary data to be published to the wire by the implementing class.
 * 
 * @author Eric Thill
 *
 */
public abstract class SerializingPublisher implements Publisher {

	private final GrowableBuffer writeBuf = GrowableBuffer.allocate(1024);

	@Override
	public synchronized void publish(MessageType type, RID from, RID to, RID group, GrowableBuffer msg) {
		switch (type) {
		case PROBE:
			sendProbe(from);
			break;
		case ANNOUNCE:
			sendAnnouncement(from);
			break;
		case BCAST_JOIN:
			sendBroadcastJoin(from, group);
			break;
		case BCAST_LEAVE:
			sendBroadcastLeave(from, group);
			break;
		case BCAST_MESSAGE:
			sendBroadcastMessage(from, group, msg);
			break;
		case UCAST_MESSAGE:
			sendUnicastMessage(from, to, msg);
			break;
		}
	}

	private void sendProbe(RID from) {
		writeBuf.reserve(9, false);
		writeBuf.direct().putByte(0, MessageType.PROBE.getValue());
		writeBuf.direct().putLong(1, from.toValue());
		writeBuf.length(9);
		send(writeBuf);
	}

	private void sendAnnouncement(RID from) {
		writeBuf.reserve(9, false);
		writeBuf.direct().putByte(0, MessageType.ANNOUNCE.getValue());
		writeBuf.direct().putLong(1, from.toValue());
		writeBuf.length(9);
		send(writeBuf);
	}

	private void sendBroadcastJoin(RID from, RID group) {
		writeBuf.reserve(17, false);
		writeBuf.direct().putByte(0, MessageType.BCAST_JOIN.getValue());
		writeBuf.direct().putLong(1, from.toValue());
		writeBuf.direct().putLong(9, group.toValue());
		writeBuf.length(17);
		send(writeBuf);
	}

	private void sendBroadcastLeave(RID from, RID group) {
		writeBuf.reserve(17, false);
		writeBuf.direct().putByte(0, MessageType.BCAST_LEAVE.getValue());
		writeBuf.direct().putLong(1, from.toValue());
		writeBuf.direct().putLong(9, group.toValue());
		writeBuf.length(17);
		send(writeBuf);
	}

	private void sendBroadcastMessage(RID from, RID group, GrowableBuffer msg) {
		int len = 17 + msg.length();
		writeBuf.reserve(len, false);
		writeBuf.direct().putByte(0, MessageType.BCAST_MESSAGE.getValue());
		writeBuf.direct().putLong(1, from.toValue());
		writeBuf.direct().putLong(9, group.toValue());
		writeBuf.direct().putBytes(17, msg.bytes(), 0, msg.length());
		writeBuf.length(len);
		send(writeBuf);
	}

	private void sendUnicastMessage(RID from, RID to, GrowableBuffer msg) {
		int len = 17 + msg.length();
		writeBuf.reserve(len, false);
		writeBuf.direct().putByte(0, MessageType.UCAST_MESSAGE.getValue());
		writeBuf.direct().putLong(1, from.toValue());
		writeBuf.direct().putLong(9, to.toValue());
		writeBuf.direct().putBytes(17, msg.bytes(), 0, msg.length());
		writeBuf.length(len);
		send(writeBuf);
	}

	/**
	 * Publish the given binary message to the wire.
	 * 
	 * @param writeBuf
	 *            The buffer to publish
	 */
	protected abstract void send(GrowableBuffer writeBuf);
}
