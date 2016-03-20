package rook.api.router.disruptor;

import java.nio.ByteBuffer;

import com.lmax.disruptor.EventFactory;

import rook.api.RID;
import rook.api.transport.GrowableBuffer;
import rook.api.transport.simple.MessageType;

/**
 * Entry for the {@link DisruptorRouter}'s RingBuffer
 * 
 * @author Eric Thill
 *
 */
class MessageEvent {

	public static EventFactory<MessageEvent> newFactory(int defaultSize) {
		return new EventFactory<MessageEvent>() {
			@Override
			public MessageEvent newInstance() {
				return new MessageEvent(defaultSize);
			}
		};
	}

	private final RID from = new RID();
	private final RID to = new RID();
	private final RID group = new RID();
	private final GrowableBuffer msg;
	private MessageType type;

	public MessageEvent(int defaultSize) {
		msg = GrowableBuffer.allocate(defaultSize);
	}
	
	public void reset() {
		from.setValue(0);
		to.setValue(0);
		group.setValue(0);
		msg.reset(false);
		type = null;
	}
	
	public void copyFrom(MessageEvent e) {
		reset();
		setFrom(e.from);
		setGroup(e.group);
		setMessage(e.msg);
		setTo(e.to);
		setType(e.type);
	}
	
	public RID getFrom() {
		return from;
	}
	
	public RID getTo() {
		return to;
	}
	
	public RID getGroup() {
		return group;
	}
	
	public GrowableBuffer getMsg() {
		return msg;
	}
	
	public MessageType getType() {
		return type;
	}
	
	public void setFrom(RID from) {
		this.from.copyFrom(from);
	}

	public void setTo(RID to) {
		this.to.copyFrom(to);
	}

	public void setGroup(RID group) {
		this.group.copyFrom(group);
	}

	public void setMessage(ByteBuffer src) {
		this.msg.setLength(0);
		this.msg.put(src);
	}
	
	public void setMessage(GrowableBuffer src) {
		this.msg.reset(false);
		this.msg.put(src);
	}
	
	public void setType(MessageType type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "MessageEvent ["
				+ "type=" + type
				+ ", from=" + from 
				+ ", to=" + to 
				+ ", group=" + group
				+ ", msg=[" + msg + "]" 
				+ "]";
	}
	
}
