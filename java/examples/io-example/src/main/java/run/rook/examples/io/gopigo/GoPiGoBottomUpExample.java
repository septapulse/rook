package run.rook.examples.io.gopigo;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import run.rook.api.RID;
import run.rook.api.Service;
import run.rook.api.config.Configurable;
import run.rook.api.exception.InitException;
import run.rook.api.transport.Transport;
import run.rook.api.util.Sleep;
import run.rook.core.io.proxy.IOProxy;

/**
 * Bottom-Up example using the default GoPiGo: Escape, Avoid, Forward
 */
public class GoPiGoBottomUpExample implements Service {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	// parsed from the configuration
	private final int escapeDistance;
	private final int avoidDistance;
	private final RID goPiGoServiceId;
	
	// create during init
	private IOProxy ioProxy;
	private MyGoPiGo robot;

	// marker to tell our outputLoop to keep running
	private volatile boolean run = true;
	
	// our bottom-up behaviors
	private final List<BottomUpBehavior> behaviors = new ArrayList<>();
	
	@Configurable
	public GoPiGoBottomUpExample(GoPiGoExampleConfig config) {
		// parse configuration values
		// note: the configuration defines defaults for everything
		this.escapeDistance = config.escapeDistance;
		this.avoidDistance = config.avoidDistance;
		this.goPiGoServiceId = new RID(config.goPiGoServiceId);
		
		// Define our behaviors in the correct order
		behaviors.add(this::escape);
		behaviors.add(this::avoid);
		behaviors.add(this::forward);
	}
	
	private boolean escape() {
		if(robot.getDistance() <= escapeDistance) {
			logger.info("Escape");
			robot.setLedLeft(true);
			robot.setLedRight(true);
			robot.setMotors(-150, -150);
			Sleep.trySleep(1000);
			robot.setMotors(-150, 150);
			Sleep.trySleep(1000);
			robot.setMotors(0, 0);
			return true;
		}
		return false;
	}

	private boolean avoid() {
		if(robot.getDistance() <= avoidDistance) {
			logger.info("Avoid");
			robot.setLedLeft(false);
			robot.setLedRight(true);
			robot.setMotors(100, 150);
			return true;
		}
		return false;
	}
	
	private boolean forward() {
		robot.setLedLeft(false);
		robot.setLedLeft(false);
		robot.setMotors(150, 150); // full speed: 255
		return true;
	}
	
	@Override
	public void setTransport(Transport transport) {
		// Create the IOProxy
		ioProxy = new IOProxy(transport, goPiGoServiceId);
	}

	@Override
	public void init() throws InitException {
		// Create MyGoPiGo robot instance
		robot = new MyGoPiGo(ioProxy);
		// Look forward
		robot.setServoPower(true);
		robot.setServoPosition(128);
		// Start control thread
		new Thread(this::controlLoop).start();
	}
	
	private void controlLoop() {
		// loop logic
		while(run) {
			// Don't kill the processor
			Sleep.trySleep(25);
			
			// try each behavior until one reports a conditions were met
			for(BottomUpBehavior b : behaviors) {
				if(b.execute()) {
					break;
				}
			}
		}
	}
	
	@Override
	public void shutdown() {
		run = false;
		ioProxy.stop();
	}

}
