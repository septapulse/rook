package rook.api.proxy;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rook.api.RID;
import rook.api.transport.GrowableBuffer;
import rook.api.util.BufferUtil;

/**
 * Sender used by a {@link ProxyService}
 * 
 * @author Eric Thill
 *
 */
public abstract class ProxySender {

	private static final long SEND_EXCEPTION_SUPPRESSION_INTERVAL = 10000;
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private long lastSendExceptionTime;
	private int numSendExceptions;

	/**
	 * Can be overwritten by implementing class to reuse a single buffer
	 * 
	 * @param requiredCapacity
	 *            The payload size (i.e. the minimum required size of the buffer
	 *            being returned)
	 * @return The buffer that will be populated and called to send(message,
	 *         length);
	 */
	protected byte[] getBuffer(int requiredCapacity) {
		return new byte[requiredCapacity];
	}

	public abstract void send(byte[] message, int length) throws Exception;

	protected void publish(byte[] message, int length) {
		try {
			if (logger.isDebugEnabled()) {
				logger.debug("Publishing: " + Arrays.toString(Arrays.copyOf(message, length)));
			}
			send(message, length);
		} catch (Throwable t) {
			logSendError(t);
		}
	}

	public synchronized void sendUnicastMessage(RID from, RID to, GrowableBuffer payload) {
		byte[] message = getBuffer(17 + payload.getLength());
		message[0] = ProxyConstants.TYPE_UNICAST_MESSAGE;
		BufferUtil.writeLong(message, 1, from.toValue());
		BufferUtil.writeLong(message, 9, to.toValue());
		BufferUtil.writeBuf(message, 17, payload.getBytes(), 0, payload.getLength());
		publish(message, message.length);
	}

	public synchronized void sendAnnouncement(RID id) {
		byte[] message = getBuffer(9);
		message[0] = ProxyConstants.TYPE_ANNOUNCEMENT;
		BufferUtil.writeLong(message, 1, id.toValue());
		publish(message, message.length);
	};

	public synchronized void sendJoin(RID service, RID group) {
		byte[] message = getBuffer(17);
		message[0] = ProxyConstants.TYPE_BROADCAST_JOIN;
		BufferUtil.writeLong(message, 1, service.toValue());
		BufferUtil.writeLong(message, 9, group.toValue());
		publish(message, message.length);
	}

	public synchronized void sendLeave(RID from, RID group) {
		byte[] message = getBuffer(17);
		message[0] = ProxyConstants.TYPE_BROADCAST_LEAVE;
		BufferUtil.writeLong(message, 1, from.toValue());
		BufferUtil.writeLong(message, 9, group.toValue());
		publish(message, message.length);
	}

	public synchronized void sendBroadcastMessage(RID from, RID group, GrowableBuffer payload) {
		byte[] message = getBuffer(17 + payload.getLength());
		message[0] = ProxyConstants.TYPE_BROADCAST_MESSAGE;
		BufferUtil.writeLong(message, 1, from.toValue());
		BufferUtil.writeLong(message, 9, group.toValue());
		BufferUtil.writeBuf(message, 17, payload.getBytes(), 0, payload.getLength());
		publish(message, message.length);
	}

	public synchronized void sendResendRequest() {
		byte[] message = getBuffer(1);
		message[0] = ProxyConstants.TYPE_RESEND_REQUEST;
		publish(message, message.length);
	}

	private void logSendError(Throwable t) {
		long now = System.currentTimeMillis();
		if (now > lastSendExceptionTime + SEND_EXCEPTION_SUPPRESSION_INTERVAL) {
			if (numSendExceptions > 0) {
				logger.error("Could not send MQTT message (suppressed " + numSendExceptions + " other send exceptions)",
						t);
			} else {
				logger.error("Could not send MQTT message", t);
			}
			lastSendExceptionTime = now;
			numSendExceptions = 0;
		} else {
			numSendExceptions++;
		}
	}
}
