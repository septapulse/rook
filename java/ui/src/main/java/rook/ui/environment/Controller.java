package rook.ui.environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
class Controller {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final File workingDirectory;
	private final Consumer<String> logListener;
	private Process process;
	
	public Controller(final File workingDirectory, final Consumer<String> logListener) {
		this.workingDirectory = workingDirectory;
		this.logListener = logListener;
	}
	
	public synchronized void start(final String... configs) throws IOException {
		try {
			stop();
			
			// FIXME sanitize input
			StringBuilder exec = new StringBuilder();
			exec.append("java -cp \"api/*\" rook.api.RookRunner");
			for(String config : configs) {
				exec.append(" \"").append(config).append("\"");
			}

			String execString = exec.toString();
			this.logger.info("Starting Rook Environment");
			this.logger.info("Executing: " + execString);
			
			List<String> command = new ArrayList<>();
			command.add("java");
			command.add("-cp");
			command.add("api/*");
			command.add("rook.api.RookRunner");
			for(String config : configs) {
				command.add(config);
			}
			ProcessBuilder pb = new ProcessBuilder(command.toArray(new String[command.size()]));
			pb.directory(workingDirectory);
			process = pb.start();
			
//			process = Runtime.getRuntime().exec(execString);
//			process.waitFor();

			this.logger.info("Started Rook Environment");
			
			new Thread(new LogReader(logListener, process.getInputStream())).start();
			new Thread(new LogReader(logListener, process.getErrorStream())).start();
		} catch(IOException e) {
			process = null;
			throw e;
		} catch(Throwable t) {
			process = null;
			throw new IOException(t);
		}
	}
	
	public synchronized void stop() {
		if(isRunning()) {
			this.logger.info("Stopping Rook Environment");
			// ask the process to stop
			process.destroy();
			
			// wait for process to stop
			long start = System.currentTimeMillis();
			while(process.isAlive() && System.currentTimeMillis()-start < 5000) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
			}
			
			// force kill a rouge process
			if(process.isAlive()) {
				this.logger.warn("Rook Environment did not shut down gracefull. Forcibly destroying process.");
				process.destroyForcibly();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {

				}
				this.logger.info("Force Kill Success: " + !process.isAlive());
			}
			
			this.logger.info("Rook Environment Stopped");
		}
	}
	
	public synchronized boolean isRunning() {
		return process != null && process.isAlive();
	}
	
	private static class LogReader implements Runnable {
		private final Consumer<String> logListener;
		private final InputStream in;
		public LogReader(Consumer<String> logListener, InputStream in) {
			this.logListener = logListener;
			this.in = in;
		}
		@Override
		public void run() {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		    String line;			
		    try {
				while ((line = reader.readLine()) != null) {
					logListener.accept(line);
				}
			} catch (IOException e) {
				// ignore
			}
		}
	}
}
