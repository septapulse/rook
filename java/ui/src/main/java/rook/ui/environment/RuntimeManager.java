package rook.ui.environment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.septapulse.rook.api.RouterLauncher;
import io.septapulse.rook.api.ServiceLauncher;
import io.septapulse.rook.api.transport.bridge.TransportBridge;
import io.septapulse.rook.api.util.FileUtil;
import rook.ui.websocket.message.ProcessRunInfo;

/**
 * Starts/Stops/Watches Service/Router/Bridge processes
 * 
 * @author Eric Thill
 *
 */
@Deprecated
public class RuntimeManager {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final AtomicLong nextUid = new AtomicLong();
	private final Class<?> transportType;
	private final String transportConfig;
	private final File jreDir;
	private final File apiDir;
	private final File runDir;
	private final File[] servicesDirs;
	private final File[] cfgDirs;
	private final Map<Long, ProcessContext> runningProcesses = Collections.synchronizedMap(new LinkedHashMap<>());

	public RuntimeManager(Class<?> transportType, String transportConfig, File jreDir, File apiDir, File runDir,
			File[] servicesDirs, File[] cfgDirs) {
		this.transportType = transportType;
		this.transportConfig = transportConfig;
		this.jreDir = jreDir;
		this.apiDir = apiDir;
		this.runDir = runDir;
		this.servicesDirs = servicesDirs;
		this.cfgDirs = cfgDirs;
		for (File f : runDir.listFiles()) {
			logger.info("Removing lingering run directory: " + f.getAbsolutePath());
			FileUtil.delete(f);
		}
	}
	
	public void stop() {
		for(ProcessContext p : runningProcesses.values()) {
			p.close();
		}
	}
	
	public void stopForcibly() {
		for(ProcessContext p : runningProcesses.values()) {
			p.closeProcessForcibly();
		}
	}

	public ProcessRunInfo startRouter(String pkg, String type, String config) throws IOException {
		String javaCmd = getJavaCmd();
		String[] params = new String[] { javaCmd, "-cp", apiDir.getAbsolutePath() + "/*",
				"-Dorg.slf4j.simpleLogger.showShortLogName=true", 
				RouterLauncher.class.getName(), "-t", type, "-c", config };
		return startProcess(params, pkg, type);
	}

	public ProcessRunInfo startTransportBridge(String pkg, String type, String config) throws IOException {
		String javaCmd = getJavaCmd();
		String[] params = new String[] { javaCmd, "-cp", apiDir.getAbsolutePath() + "/*",
				"-Dorg.slf4j.simpleLogger.showShortLogName=true", 
				TransportBridge.class.getName(), "-t1", transportType.getName(), "-c1", transportConfig, "-t2", type,
				"-c2", config };
		return startProcess(params, pkg, type);
	}

	public ProcessRunInfo startService(String pkg, String sid, String cfg) throws IOException {
		File libDir = findServiceLibDir(pkg);
		File cfgDir = findServiceCfgDir(pkg, sid, cfg);
		File cfgFile = new File(cfgDir, "cfg");
		File idFile = new File(cfgDir, "id");
		if (!libDir.isDirectory() || !cfgFile.isFile() || !idFile.isFile())
			return null;
		String classpath = apiDir.getAbsolutePath() + "/*:" + libDir.getAbsolutePath() + "/*";
		String id = FileUtil.readFully(idFile).trim();
		String cfgUrl = "file://" + cfgFile.getAbsolutePath();
		return launchService(classpath, pkg, cfg, id, sid, cfgUrl);
	}

	private ProcessRunInfo launchService(String classpath, String pkg, String cfgName, String id, String type,
			String cfgUrl) throws IOException {
		String javaCmd = getJavaCmd();
		String[] params = new String[] { javaCmd, "-cp", classpath, 
				"-Dorg.slf4j.simpleLogger.showShortLogName=true", 
				ServiceLauncher.class.getName(), "-id", id, "-st",
				type, "-sc", cfgUrl, "-tt", transportType.getName(), "-tc", transportConfig };
		return startProcess(params, pkg, cfgName);
	}

	private String getJavaCmd() {
		return jreDir == null ? "java" : jreDir.getAbsolutePath() + "/bin/java";
	}

	public ProcessRunInfo startProcess(String[] params, String pkg, String name) throws IOException {
		long uid = getNextUid();
		File uidDir = new File(runDir, Long.toString(uid));
		uidDir.mkdirs();
		File logFile = new File(uidDir, "log");
		ProcessBuilder pb = new ProcessBuilder(params);
		pb.redirectOutput(logFile);
		pb.redirectError(logFile);
		Process p = pb.start();
		runningProcesses.put(uid, new ProcessContext(pkg, name, uid, uidDir, p));
		FileUtil.replaceFileContents(new File(uidDir, "pkg"), pkg);
		FileUtil.replaceFileContents(new File(uidDir, "name"), name);
		return new ProcessRunInfo().setPkg(pkg).setName(name).setUid(uid);
	}

	private long getNextUid() {
		return nextUid.getAndIncrement();
	}

	private File findServiceLibDir(String pkg) {
		for (File serviceDir : servicesDirs) {
			File libDir = new File(serviceDir, "java/" + pkg + "/lib/");
			if (libDir.isDirectory()) {
				return libDir;
			}
		}
		return null;
	}

	private File findServiceCfgDir(String pkg, String sid, String cfg) {
		for (File cfgDir : cfgDirs) {
			File instanceDir = new File(cfgDir, pkg + "/" + sid + "/" + cfg);
			if (instanceDir.isDirectory()) {
				File cfgFile = new File(instanceDir, "cfg");
				File idFile = new File(instanceDir, "id");
				if (instanceDir.isDirectory() && cfgFile.isFile() && idFile.isFile()) {
					return instanceDir;
				}
			}
		}
		return null;
	}

	public ProcessRunInfo stop(long uid) throws InterruptedException {
		ProcessContext context = runningProcesses.remove(uid);
		if (context == null) {
			return null;
		}
		context.close();
		return new ProcessRunInfo().setPkg(context.getPkg()).setName(context.getName()).setUid(context.getUid());
	}

	public boolean addLogConsumer(long uid, LogSender logSender) {
		ProcessContext context = runningProcesses.get(uid);
		if (context == null) {
			return false;
		}
		return context.addLogSender(logSender);
	}

	public List<ProcessRunInfo> getRunningServices() {
		List<ProcessRunInfo> result = new ArrayList<>();
		if (runDir.isDirectory()) {
			for (File instanceDir : runDir.listFiles()) {
				if (instanceDir.isDirectory()) {
					File pkgFile = new File(instanceDir, "pkg");
					File nameFile = new File(instanceDir, "name");
					if (pkgFile.isFile() && nameFile.isFile()) {
						try {
							long uid = Long.parseLong(instanceDir.getName());
							String pkg = FileUtil.readFully(pkgFile).trim();
							String name = FileUtil.readFully(nameFile).trim();
							result.add(new ProcessRunInfo().setUid(uid).setPkg(pkg).setName(name));
						} catch (Throwable t) {
							logger.error("Could not read run/<instance> directory: " + instanceDir.getAbsolutePath(),
									t);
						}
					}
				}
			}
		}
		return result;
	}
}
