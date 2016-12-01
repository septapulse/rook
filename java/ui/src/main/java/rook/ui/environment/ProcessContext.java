package rook.ui.environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rook.api.util.FileUtil;

@Deprecated
class ProcessContext {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final String pkg;
	private final String name;
	private final long uid;
	private final File dir;
	private final Process process;
	private final List<LogReader> logReaders = Collections.synchronizedList(new ArrayList<>());
	private volatile boolean open = true;
	
	public ProcessContext(String pkg, String name, long uid, File dir, Process process) {
		this.pkg = pkg;
		this.name = name;
		this.uid = uid;
		this.dir = dir;
		this.process = process;
	}

	public synchronized void close() {
		if(open) {
			open = false;
			process.destroy();
			try {
				process.waitFor(250, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// move on
			}
			if(process.isAlive()) {
				logger.info("Forcibly destroying " + name);
				process.destroyForcibly();
			}
			synchronized (logReaders) {
				for(LogReader r : logReaders) {
					r.stop();
				}
			}
			logReaders.clear();
			FileUtil.delete(dir);
		}
	}
	
	public synchronized void closeProcessForcibly() {
		process.destroyForcibly();
	}

	public synchronized boolean addLogSender(LogSender logSender) {
		if(open) {
			LogReader r = new LogReader(new File(dir, "log"), logSender);
			r.start();
			logReaders.add(r);
			return true;
		} else {
			return false;
		}
	}
	
	public String getPkg() {
		return pkg;
	}
	
	public String getName() {
		return name;
	}
	
	public long getUid() {
		return uid;
	}
}