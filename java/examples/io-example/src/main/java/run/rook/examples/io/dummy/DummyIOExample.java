package run.rook.examples.io.dummy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import run.rook.api.RID;
import run.rook.api.Service;
import run.rook.api.config.Configurable;
import run.rook.api.exception.InitException;
import run.rook.api.transport.Transport;
import run.rook.api.util.Sleep;
import run.rook.core.io.proxy.IOProxy;
import run.rook.core.io.proxy.message.IOValue;

public class DummyIOExample implements Service {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	// parsed from the configuration
	private final long outputInterval;
	private final RID ioServiceId;
	private final RID outputId;
	
	// create during init
	private IOProxy ioProxy;
	private Transport transport;

	// marker to tell our outputLoop to keep running
	private volatile boolean run = true;
	
	@Configurable
	public DummyIOExample(DummyIOExampleServiceConfig config) {
		this.outputInterval = config.outputInterval;
		this.ioServiceId = RID.create(config.ioServiceId);
		this.outputId = RID.create(config.outputId);
	}
	
	@Override
	public void setTransport(Transport transport) {
		this.transport = transport;
	}

	@Override
	public void init() throws InitException {
		// Listen for capabilities
		logger.info("IO Capabilities: " + IOProxy.probe(transport, 1000).get(ioServiceId));

		// Create the IOProxy
		ioProxy = new IOProxy(transport, ioServiceId);
		
		// Listen for inputs
		ioProxy.inputs().addConsumer(this::onInput);
		
		// Start thread to change outputs
		if(outputId != null)
			new Thread(this::outputLoop).start();
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
