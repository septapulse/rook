package rook.examples.io.dummy;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rook.api.RID;
import rook.api.Service;
import rook.api.config.Configurable;
import rook.api.exception.InitException;
import rook.api.transport.Transport;
import rook.api.util.Sleep;
import rook.core.io.proxy.IOProxy;
import rook.core.io.proxy.message.Cap;
import rook.core.io.proxy.message.IOValue;

public class DummyIOExample implements Service {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	// parsed from the configuration
	private final long outputInterval;
	private final RID outputId;
	
	// create during init
	private IOProxy ioProxy;
	private Transport transport;

	// marker to tell our outputLoop to keep running
	private volatile boolean run = true;
	
	@Configurable
	public DummyIOExample(DummyIOExampleServiceConfig config) {
		this.outputInterval = config.outputInterval;
		this.outputId = config.outputId == null ? null : RID.create(config.outputId);
	}
	
	@Override
	public void setTransport(Transport transport) {
		this.transport = transport;
	}

	@Override
	public void init() throws InitException {
		// Create the IOProxy
		ioProxy = new IOProxy(transport);

		// Listen for capabilities
		ioProxy.caps().addConsumer(this::onCaps);
		
		// Request capabilities now
		ioProxy.caps().requestCaps();
		
		// Listen for inputs
		ioProxy.inputs().addConsumer(this::onInput);
		
		// Start thread to change outputs
		if(outputId != null)
			new Thread(this::outputLoop).start();
	}
	
	private void onCaps(List<Cap> caps) {
		logger.info("IO Capabilities: " + caps);
	}
	
	private void onInput(RID id, IOValue value) {
		logger.info("IO Input: " + id + " = " + value);
	}
	
	private void outputLoop() {
		boolean value = false;
		
		// loop
		while(run) {
			// wait a bit before updating again
			Sleep.trySleep(outputInterval);
			
			// flip output value
			value = !value;
			
			// send new output value for "outputId" to IOService
			logger.info("Setting IO Output " + outputId + "="+ value);
			ioProxy.outputs().setOutput(
					outputId, new IOValue().setValue(value)
			);
			
		}
	}
	
	@Override
	public void shutdown() {
		run = false;
		ioProxy.stop();
	}

}
