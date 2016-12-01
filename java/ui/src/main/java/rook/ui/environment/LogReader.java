package rook.ui.environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
class LogReader {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final AtomicBoolean run = new AtomicBoolean(false);
	private final File file;
	private final LogSender sender;
	private final BlockingQueue<String> lineQueue = new ArrayBlockingQueue<>(16);
	private BufferedReader reader;
	
	public LogReader(File file, LogSender sender) {
		this.file = file;
		this.sender = sender;
	}
	
	public void start() {
		if(run.compareAndSet(false, true)) {
			new Thread(this::dispatchLoop, "LogDispatcher").start();
			new Thread(this::readLoop, "LogReader").start();
		}
	}
	
	public void stop() {
		if(run.compareAndSet(true, false)) {
			lineQueue.offer("");
		}
	}
	
	private void dispatchLoop() {
		try {
			while(run.get()) {
				String line = lineQueue.poll(5, TimeUnit.SECONDS);
				if(!sender.isOpen()) {
					stop();
				} else if(line != null) {
					sender.send(line);
				}
			}
		} catch (Throwable t) {
			stop();
		}
	}

	private void readLoop() {
		// wait for the file to exist
		while (run.get() && !file.exists()) {
			trySleep(1000);
		}
		reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));

			while (run.get()) {
				String line;
				while ((line = reader.readLine()) != null) {
					lineQueue.put(line);
				}
			}
		} catch(Throwable t) {
			stop();
		} finally {
			if (reader != null) {
				logger.info("Closing " + file + " LogReader");
				try {
					reader.close();
				} catch (IOException e) {

				}
			}
		}
	}

	private void trySleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// ignore
		}
	}
}