package io.septapulse.rook.examples.io.gopigo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.septapulse.rook.api.Service;
import io.septapulse.rook.api.config.Configurable;
import io.septapulse.rook.api.exception.InitException;
import io.septapulse.rook.api.transport.Transport;
import io.septapulse.rook.api.util.Sleep;
import io.septapulse.rook.core.io.proxy.IOProxy;

/**
 * If you get too close to the GoPiGo's ultrasonic distance sensor, you'll scare it backwards.
 */
public class GoPiGoExample implements Service {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	// parsed from the configuration
	private final int escapeDistance;
	
	// create during init
	private IOProxy ioProxy;
	private MyGoPiGo robot;

	// marker to tell our outputLoop to keep running
	private volatile boolean run = true;
	
	@Configurable
	public GoPiGoExample(GoPiGoExampleServiceConfig config) {
		this.escapeDistance = config.escapeDistance;
	}
	
	@Override
	public void setTransport(Transport transport) {
		// Create the IOProxy
		ioProxy = new IOProxy(transport);
	}

	@Override
	public void init() throws InitException {
		// Create MyGoPiGo robot instance
		robot = new MyGoPiGo(ioProxy);
		
		// Start control thread
		new Thread(this::controlLoop).start();
	}
	
	private void controlLoop() {
		// loop logic
		while(run) {
			// don't kill the processor. check 10 times per second.
			Sleep.trySleep(100);
			
			if(robot.getDistance() <= escapeDistance) {
				logger.info("Ah! Too close! Backing up...");
				robot.setLedLeft(true);
				robot.setLedRight(true);
				robot.setMotors(-150, -150);
				Sleep.trySleep(1000);
				robot.setMotors(0, 0);
				robot.setLedLeft(false);
				robot.setLedRight(false);
				Sleep.trySleep(1000);
				if(robot.getDistance() > escapeDistance) {
					logger.info("Phew!");
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
