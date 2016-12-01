package rook.core.transport.aeron;

import java.io.File;

import io.aeron.driver.MediaDriver;
import rook.api.Router;
import rook.api.config.Configurable;
import rook.api.exception.InitException;

/**
 * A {@link Router} that spins up an Aeron {@link MediaDriver}
 * 
 * @author Eric Thill
 *
 */
public class AeronRouter implements Router {

	private final Thread shutdownTask = new Thread(this::stop);
	private final String directoryName;
	private MediaDriver.Context context;
	private MediaDriver driver;

	@Configurable
	public AeronRouter(AeronRouterConfig config) {
		this.directoryName = config.getDirectoryName();
	}

	@Override
	public synchronized void start() throws InitException {
		if (driver == null) {
			if (new File(directoryName).exists()) {
				throw new InitException("Aeron directory already exists");
			}
			context = new MediaDriver.Context();
			context.aeronDirectoryName(directoryName);
			driver = MediaDriver.launch(context);
			Runtime.getRuntime().addShutdownHook(shutdownTask);
		}
	}

	@Override
	public synchronized void stop() {
		if (driver != null) {
			try {
				Runtime.getRuntime().removeShutdownHook(shutdownTask);
			} catch (Throwable t) {

			}
			delete(new File(context.aeronDirectoryName()));
			driver.close();
			context.close();
			driver = null;
			context = null;
		}
	}

	private static void delete(File f) {
		if (!f.exists())
			return;
		if (f.isDirectory()) {
			for (File child : f.listFiles()) {
				delete(child);
			}
		}
		f.delete();
	}

}
