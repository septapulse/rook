package rook.api.router.disruptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import rook.api.RID;
import rook.api.Service;
import rook.api.transport.AnnounceTransport;
import rook.api.transport.BroadcastTransport;
import rook.api.transport.Transport;
import rook.api.transport.UnicastTransport;
import rook.api.transport.simple.Publisher;
import rook.api.transport.simple.SimpleAnnounceTransport;
import rook.api.transport.simple.SimpleBroadcastTransport;
import rook.api.transport.simple.SimpleUnicastTransport;

/**
 * A {@link Transport} for a {@link Disruptor}
 * 
 * @author Eric Thill
 *
 */
public class DisruptorTransport implements Transport, EventHandler<MessageEvent> {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final RID serviceId;
	private final Service service;
	private final ClassLoader serviceClassLoader;
	private final SimpleAnnounceTransport announce;
	private final SimpleBroadcastTransport bcast;
	private final SimpleUnicastTransport ucast;
	
	public DisruptorTransport(RID serviceId, Service service, ClassLoader serviceClassLoader,
			RingBuffer<MessageEvent> ringBuffer, int defaultMessageSize) {
		this.serviceId = serviceId;
		this.service = service;
		this.serviceClassLoader = serviceClassLoader;
		Publisher publisher = new DisruptorPublisher(ringBuffer);
		announce = new SimpleAnnounceTransport(serviceId, publisher);
		bcast = new SimpleBroadcastTransport(serviceId, publisher, defaultMessageSize);
		ucast = new SimpleUnicastTransport(serviceId, publisher, defaultMessageSize);
	}
	
	@Override
	public AnnounceTransport announce() {
		return announce;
	}
	
	@Override
	public BroadcastTransport bcast() {
		return bcast;
	}
	
	@Override
	public UnicastTransport ucast() {
		return ucast;
	}
	
	@Override
	public void onEvent(MessageEvent event, long sequence, boolean endOfBatch) throws Exception {
		if(event.getType() == null) {
			// initialize event
			Thread.currentThread().setName(serviceId.toString());
			Thread.currentThread().setContextClassLoader(serviceClassLoader);
			announce.incognito_announce(serviceId);
			service.init();
			return;
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug(serviceId + " handling: " + event);
		}
		
		switch(event.getType()) {
		case BCAST_JOIN:
			bcast.handleBcastJoin(event.getFrom(), event.getGroup());
			break;
		case BCAST_LEAVE:
			bcast.handleBcastLeave(event.getFrom(), event.getGroup());
			break;
		case BCAST_MESSAGE:
			bcast.handleBcastMessage(event.getFrom(), event.getGroup(), event.getMsg());
			break;
		case UCAST_MESSAGE:
			ucast.handleUcastMessage(event.getFrom(), event.getTo(), event.getMsg());
			break;
		case ANNOUNCE:
			announce.handleAnnouncement(event.getFrom());
			break;
		case PROBE:
			announce.handleProbe(event.getFrom());
			break;
		}
	}
	
	@Override
	public String toString() {
		return "DisruptorTransport [" + serviceId + "]";
	}

}
