package io.septapulse.rook.core.transport.aeron;

import org.agrona.DirectBuffer;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;

import io.aeron.FragmentAssembler;
import io.aeron.Subscription;
import io.aeron.logbuffer.FragmentHandler;
import io.aeron.logbuffer.Header;
import io.septapulse.rook.api.transport.simple.DeserializingDispatcher;
import io.septapulse.rook.api.transport.simple.SimpleAnnounceTransport;
import io.septapulse.rook.api.transport.simple.SimpleBroadcastTransport;
import io.septapulse.rook.api.transport.simple.SimpleUnicastTransport;

/**
 * 
 * @author Eric Thill
 *
 */
class AeronReceiver {

	private final DeserializingDispatcher dispatcher;
	private final Subscription sub;
	private final IdleStrategy idleStrategy;
	private volatile boolean run;
	
	public AeronReceiver(Subscription sub,
			SimpleAnnounceTransport announce, 
			SimpleBroadcastTransport bcast,
			SimpleUnicastTransport ucast) {
		this.sub = sub;
		dispatcher = new DeserializingDispatcher(announce, bcast, ucast);
		idleStrategy = new BackoffIdleStrategy(100, 10, 10000, 1000000); // FIXME configurable
	}

	public void start() {
		run = true;
		new Thread(this::receiveLoop, getClass().getSimpleName()).start();
	}
	
	public void stop() {
		run = false;
	}
	
	private void receiveLoop() {
		final FragmentAssembler fragmentAssembler = new FragmentAssembler(fragmentHandler);
		while(run) {
			idleStrategy.idle(sub.poll(fragmentAssembler, 10));
		}
	}
	
	private final FragmentHandler fragmentHandler = new FragmentHandler() {
		@Override
		public void onFragment(DirectBuffer buffer, int offset, int length, Header header) {
			dispatcher.dispatch(buffer, offset, length);
		}
	};
	
}
