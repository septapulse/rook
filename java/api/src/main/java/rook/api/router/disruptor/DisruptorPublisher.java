package rook.api.router.disruptor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.RingBuffer;

import rook.api.RID;
import rook.api.transport.GrowableBuffer;
import rook.api.transport.simple.MessageType;
import rook.api.transport.simple.Publisher;

/**
 * A {@link Publisher} that is able to publish to a {@link DisruptorRouter}
 * 
 * @author Eric Thill
 *
 */
class DisruptorPublisher implements Publisher {

	private final RingBuffer<MessageEvent> ringBuffer;
	private final BlockingQueue<MessageEvent> overflowQueue = new LinkedBlockingQueue<>();

	/**
	 * DisruptorPublisher constructor
	 * 
	 * @param ringBuffer The disruptor's ringBuffer
	 */
	public DisruptorPublisher(RingBuffer<MessageEvent> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}

	@Override
	public synchronized void publish(MessageType type, RID from, RID to, RID group, GrowableBuffer msg) {
		MessageEvent e;
		boolean overflow;
		long seq = 0;

		if (overflowQueue.size() > 0) {
			// already in overflow mode.
			e = new MessageEvent(0);
			overflow = true;
		} else {
			try {
				seq = ringBuffer.tryNext();
				e = ringBuffer.get(seq);
				e.reset();
				overflow = false;
			} catch (InsufficientCapacityException ex) {
				// prevent potential deadlock
				e = new MessageEvent(0);
				overflow = true;
			}
		}

		e.reset();
		e.setType(type);
		if (from != null)
			e.setFrom(from);
		if (to != null)
			e.setTo(to);
		if (group != null)
			e.setGroup(group);
		if (msg != null)
			e.setMessage(msg);

		if (overflow) {
			overflowQueue.add(e);
			if (overflowQueue.size() == 1) {
				// first element in queue added: spin up new thread to handle
				// overflow
				new Thread(overflowQueueDispatcher).start();
			}
		} else {
			ringBuffer.publish(seq);
		}
	}

	private synchronized int processNextOverflow() throws InsufficientCapacityException {
		if (overflowQueue.size() > 0) {
			// try to grab an available place in the ringBuffer
			long seq = ringBuffer.tryNext();
			// a space was reserved: copy to it and publish
			ringBuffer.get(seq).copyFrom(overflowQueue.poll());
			ringBuffer.publish(seq);
		}
		return overflowQueue.size();
	}

	private final Runnable overflowQueueDispatcher = new Runnable() {
		@Override
		public void run() {
			while (true) {
				try {
					if (processNextOverflow() == 0) {
						// no longer needed: spin-down the thread
						return;
					}
				} catch (InsufficientCapacityException e) {
					// back off for a bit, then try again
					try {
						Thread.sleep(10);
					} catch (InterruptedException e1) {

					}
				}
			}
		}
	};
}
