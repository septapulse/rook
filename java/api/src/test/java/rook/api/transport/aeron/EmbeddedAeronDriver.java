package rook.api.transport.aeron;

import java.io.File;

import io.aeron.driver.MediaDriver;
import rook.api.util.FileUtil;

public class EmbeddedAeronDriver {

	private MediaDriver driver;
	
	public synchronized MediaDriver start() {
		stop();
		driver = MediaDriver.launchEmbedded();
		return driver;
	}
	
	public synchronized void stop() {
		if(driver != null) {
			File dir = new File(driver.aeronDirectoryName());
			driver.close();
			FileUtil.delete(dir); 
			driver = null;
		}
	}
	
}
