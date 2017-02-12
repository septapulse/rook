package run.rook.examples.io.gopigo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import run.rook.api.RID;
import run.rook.api.Service;
import run.rook.api.ServiceLauncher;
import run.rook.api.exception.InitException;
import run.rook.api.transport.Transport;
import run.rook.core.io.proxy.IOProxy;
import run.rook.core.io.proxy.message.IOValue;

/**
 * If you get too close to the GoPiGo's ultrasonic distance sensor, the LED's will light up.
 */
public class GoPiGoSimpleExample implements Service {

	private static final String MY_ROBOT_IP = "192.168.99.3";
	
	public static void main(String[] args) {
		ServiceLauncher.main(
				"-id", "MY_SERVICE",
				"-st", "run.rook.examples.io.gopigo.GoPiGoSimpleExample",
				"-sc", "{}",
				"-tt", "run.rook.core.transport.websocket.WebsocketTransport",
				"-tc", "{\"url\":\"ws://"+MY_ROBOT_IP+":8080\"}");
	}
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	// ID's of inputs and outputs
	private final RID ledIdLeft = new RID("LED_LEFT");
	private final RID ledIdRight = new RID("LED_RIGHT");
	private final RID distanceId = new RID("DISTANCE");
	
	// create using the Transport
	private IOProxy ioProxy;
	
	// keep track of the current LED Status
	private boolean curLedStatus = false;

	@Override
	public void setTransport(Transport transport) {
		// Create the IOProxy to the "IO" service, which takes care of all underlying communication.
		ioProxy = new IOProxy(transport, new RID("IO"));
	}

	@Override
	public void init() throws InitException {
		// Listen for "distance" updates
		ioProxy.inputs().addFilteringConsumer(distanceId, this::handleDistance);
	}
	
	void handleDistance(RID id, IOValue value) {
		logger.info("Distance is " + value);
		boolean newLedStatus = value.getValueAsInt() < 30;
		if(newLedStatus != curLedStatus) {
			ioProxy.outputs().setOutput(ledIdLeft, new IOValue(newLedStatus));
			ioProxy.outputs().setOutput(ledIdRight, new IOValue(newLedStatus));
			curLedStatus = newLedStatus;
		}
	}
	
	@Override
	public void shutdown() {
		ioProxy.stop();
	}

}
