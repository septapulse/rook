package rook.core.transport.aeron;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.aeron.Aeron;
import io.aeron.Aeron.Context;
import io.aeron.Image;
import io.aeron.Publication;
import io.aeron.Subscription;
import io.aeron.driver.MediaDriver;
import rook.api.RID;
import rook.api.config.Configurable;
import rook.api.exception.ExceptionHandler;
import rook.api.exception.InitException;
import rook.api.transport.AnnounceTransport;
import rook.api.transport.BroadcastTransport;
import rook.api.transport.ControllableTransport;
import rook.api.transport.Transport;
import rook.api.transport.UnicastTransport;
import rook.api.transport.simple.Publisher;
import rook.api.transport.simple.SimpleAnnounceTransport;
import rook.api.transport.simple.SimpleBroadcastTransport;
import rook.api.transport.simple.SimpleUnicastTransport;

/**
 * A {@link Transport} implementation that uses Aeron {@link Publication} and
 * {@link Subscription} to communicate with an Aeron {@link MediaDriver}
 * 
 * @author Eric Thill
 *
 */
public class AeronTransport implements ControllableTransport {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final AtomicBoolean running = new AtomicBoolean(false);
	private final AeronTransportConfig config;
	private Context context;
	private Aeron aeron;
	private Subscription subscription;
	private Publication publication;
	private SimpleAnnounceTransport announceTransport;
	private SimpleBroadcastTransport broadcastTransport;
	private SimpleUnicastTransport unicastTransport;
	private AeronReceiver receiver;
	private RID serviceId;
	private ExceptionHandler exceptionHandler;
	private boolean respondToProbes = true;

	@Configurable
	public AeronTransport(AeronTransportConfig config) {
		this.config = config;
	}

	private void onAvailableImage(Image image) {
		logger.info("Connected to Aeron Image");
	}

	private void onUnavailableImage(Image image) {
		logger.info("Disconnected from Aeron Image");
		if (running.get() && exceptionHandler != null) {
			exceptionHandler.error("Disconnected from Aeron Image", new IOException());
		}
	}

	@Override
	public void setServiceId(RID serviceId) {
		this.serviceId = serviceId.immutable();
	}

	@Override
	public void setExceptionHandler(ExceptionHandler h) {
		this.exceptionHandler = h;
	}

	@Override
	public void setRespondToProbes(boolean respondToProbes) {
		this.respondToProbes = respondToProbes;
	}

	@Override
	public synchronized void start() throws InitException {
		if (running.compareAndSet(false, true)) {
			logger.info("Connecting to " + config.getAeronDirectoryName() + " channel=" + config.getChannel()
					+ " stream=" + config.getStreamId());
			context = new Context();
			context.aeronDirectoryName(config.getAeronDirectoryName()).availableImageHandler(this::onAvailableImage)
					.unavailableImageHandler(this::onUnavailableImage);
			aeron = Aeron.connect(context);
			subscription = aeron.addSubscription(config.getChannel(), config.getStreamId());
			publication = aeron.addPublication(config.getChannel(), config.getStreamId());

			final Publisher publisher = new AeronPublisher(publication);
			announceTransport = new SimpleAnnounceTransport(serviceId, publisher, respondToProbes);
			broadcastTransport = new SimpleBroadcastTransport(serviceId, publisher);
			unicastTransport = new SimpleUnicastTransport(serviceId, publisher, null);

			receiver = new AeronReceiver(subscription, announceTransport, broadcastTransport, unicastTransport);

			receiver.start();
			if (respondToProbes)
				announceTransport.incognito_announce(serviceId);
			logger.info("Started");
		}
	}

	@Override
	public synchronized void shutdown() {
		if (running.compareAndSet(true, false)) {
			publication.close();
			subscription.close();
			aeron.close();
			context.close();
		}
	}

	@Override
	public AnnounceTransport announce() {
		return announceTransport;
	}

	@Override
	public BroadcastTransport bcast() {
		return broadcastTransport;
	}

	@Override
	public UnicastTransport ucast() {
		return unicastTransport;
	}

}
