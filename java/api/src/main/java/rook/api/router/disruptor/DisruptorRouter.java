package rook.api.router.disruptor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import rook.api.InitException;
import rook.api.RID;
import rook.api.Router;
import rook.api.Service;
import rook.api.util.Parse;

/**
 * {@link Router} implementation that uses the Disruptor framework to route
 * messages between services.
 * 
 * @author Eric Thill
 *
 */
public class DisruptorRouter implements Router {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Map<RID, Service> services = new LinkedHashMap<>();
	private final int defaultMessageSize;
	private final int ringBufferSize;
	private volatile boolean started = false;
	private ExecutorService executorService;
	private Disruptor<MessageEvent> disruptor;
	
	public DisruptorRouter() {
		this(new Properties());
	}
	
	public DisruptorRouter(Properties props) {
		defaultMessageSize = Parse.getInteger(props, "defaultMessageSize", 1024);
		ringBufferSize = Parse.getInteger(props, "ringBufferSize", 128);
	}
	
	@Override
	public synchronized void addService(RID id, Service service) throws IllegalArgumentException, IllegalStateException {
		if(started) {
			throw new IllegalStateException("Cannot add a service while the router is running");
		}
		if(services.containsKey(id)) {
			throw new IllegalArgumentException("Service with id " + id + " already exists");
		}
		services.put(id.unmodifiable(), service);
	}
	
	@Override
	public synchronized void start() throws InitException {
		if(started) {
			throw new InitException("Router is already started");
		}
		
		if(logger.isInfoEnabled())
			logger.info("Starting Router. services=" + services);
		
		executorService = Executors.newFixedThreadPool(services.size());
		disruptor = new Disruptor<>(
				MessageEvent.newFactory(defaultMessageSize), 
				ringBufferSize, 
				executorService, 
				ProducerType.MULTI, 
				new BlockingWaitStrategy());
		
		DisruptorTransport[] transports = new DisruptorTransport[services.size()];
		int i = 0;
		for(Map.Entry<RID, Service> e : services.entrySet()) {
			DisruptorTransport transport = new DisruptorTransport(
					e.getKey(), e.getValue(),
					e.getValue().getClass().getClassLoader(), 
					disruptor.getRingBuffer(), defaultMessageSize);
			e.getValue().setTransport(transport);
			transports[i++] = transport;
		}

		disruptor.handleExceptionsWith(exceptionHandler);
		disruptor.handleEventsWith(transports);
		disruptor.start();
		
		dispatchInitializeEvent(disruptor.getRingBuffer());
		
		logger.info("Router Started");
	}
	
	private final ExceptionHandler<MessageEvent> exceptionHandler = new ExceptionHandler<MessageEvent>() {
		
		@Override
		public void handleOnStartException(Throwable ex) {
			logger.error("Disruptor Startup Exception", ex);
		}
		
		@Override
		public void handleOnShutdownException(Throwable ex) {
			logger.error("Disruptor Shutdown Exception", ex);
		}
		
		@Override
		public void handleEventException(Throwable ex, long sequence, MessageEvent event) {
			if(ex instanceof InitException) {
				logger.error("Initialization Exception. Shutting down.", ex);
				shutdown();
			} else {
				logger.error("Unhandled Exception", ex);
			}
		}
	};
	
	private void dispatchInitializeEvent(RingBuffer<MessageEvent> ringBuffer) {
		long seq = ringBuffer.next();
		MessageEvent e = ringBuffer.get(seq);
		e.setType(null);
		ringBuffer.publish(seq);
	}

	@Override
	public synchronized void shutdown() {
		// FIXME orderly shutdown task in disruptor
		logger.info("Halting Disruptor");
		disruptor.halt();
		logger.info("Shutting down ExecutorService");
		executorService.shutdown();
		for(Map.Entry<RID, Service> e : services.entrySet()) {
			try {
				logger.info("Shutting down '" + e.getKey() + "' Service");
				e.getValue().shutdown();
			} catch(Throwable t) {
				logger.error("Encountered exception shutting down " + e.getKey(), t);
			}
		}
	}
}
