package rook.daemon.processes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import rook.api.util.FileUtil;
import rook.daemon.common.Result;
import rook.daemon.packages.PackageInfo;
import rook.daemon.packages.PackageManager;
import rook.daemon.packages.ServiceInfo;

@WebSocket
public class ProcessManager {
	
	public static final String PROCESS_MANAGER_PROTOCOL = "PROCESS";
	
	private static final String INFO_FILENAME = "info";
	private static final String LOG_FILENAME = "log";
	private static final int BASE_36 = 36;
	
	private final Map<String, Process> processes = Collections.synchronizedMap(new HashMap<>());
	private final AtomicLong nextProcessId = new AtomicLong(System.currentTimeMillis());
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Gson gson = new Gson();
	private final File[] packageDirs;
	private final File runtimeDir;
	
	public ProcessManager(File runtimeDir, File... packageDirs) {
		runtimeDir.mkdirs();
		this.runtimeDir = runtimeDir;
		this.packageDirs = packageDirs;
	}
	
	@OnWebSocketMessage
	public void onText(Session session, String message) throws IOException {
		ProcessManagerRequest req = gson.fromJson(message, ProcessManagerRequest.class);
		boolean success;
		ProcessInfo processInfo;
		List<ProcessInfo> processes;
		try {
			switch (req.getType()) {
			case STATUS:
				processes = getProcesses();
				success = processes != null;
				send(session, new ProcessManagerResponse()
						.setType(MessageType.STATUS)
						.setResult(new Result().setSuccess(success))
						.setProcesses(processes));
				break;
			case START:
				processInfo = start(req.getPackage(), req.getService(), req.getArguments());
				success = processInfo != null;
				send(session, new ProcessManagerResponse()
						.setType(MessageType.START)
						.setResult(new Result().setSuccess(success))
						.setProcess(processInfo));
				break;
			case STOP:
				success = stop(req.getId(), false);
				send(session, new ProcessManagerResponse()
						.setType(MessageType.STOP)
						.setResult(new Result().setSuccess(success)));
				break;
			case STOP_FORCIBLY:
				success = stop(req.getId(), true);
				send(session, new ProcessManagerResponse()
						.setType(MessageType.STOP_FORCIBLY)
						.setResult(new Result().setSuccess(success)));
				break;
			case CLEAN:
				success = clean(req.getId());
				send(session, new ProcessManagerResponse()
						.setType(MessageType.CLEAN)
						.setResult(new Result().setSuccess(success)));
				break;
			case LOG:
				String log = getLog(req.getId());
				success = log != null;
				send(session, new ProcessManagerResponse()
						.setType(MessageType.LOG)
						.setResult(new Result().setSuccess(success))
						.setLog(log));
				break;
			case LOG_STREAM:
//				implement();
				// FIXME implement
				logger.error("LOG_STREAM not implemented");
				break;
			}
		} catch(ProcessManagerException e) {
			Result result = new Result().setSuccess(false).setError(e);
			send(session, new ProcessManagerResponse()
					.setType(req.getType())
					.setResult(result));
		}
	}
	
	public boolean clean(String id) {
		if(id == null) {
			for(File dir : runtimeDir.listFiles(f -> f.isDirectory())) {
				Process p = processes.get(dir.getName());
				if(p == null || !p.isAlive()) {
					FileUtil.delete(dir);
					processes.remove(dir.getName());
				}
			}
			return true;
		} else {
			File dir = new File(runtimeDir, id);
			if(!dir.isDirectory()) {
				return false;
			}
			Process p = processes.get(id);
			if(p.isAlive()) {
				return false;
			}
			FileUtil.delete(dir);
			processes.remove(id);
			return true;
		}
	}

	public ProcessInfo start(String pkg, String service, String[] arguments) throws ProcessManagerException {
		PackageInfo packageInfo = null;
		for(File packagesDir : packageDirs) {
			File packageDir = new File(packagesDir, pkg);
			File packageFile = new File(packageDir, PackageManager.ROOK_CFG_FILENAME);
			if(packageFile.exists()) {
				try {
					packageInfo = gson.fromJson(FileUtil.readFully(packageFile), PackageInfo.class);
					if(packageInfo != null && packageInfo.getServices() != null) {
						ServiceInfo serviceInfo = packageInfo.getServices().get(service);
						if(serviceInfo != null) {
							return start(packageInfo.getName(), serviceInfo.getName(), serviceInfo.getCommand(), arguments);
						}
					}
				} catch(Throwable t) {
					throw new ProcessManagerException("Could not load package info from " + packageFile.getAbsolutePath(), t);
				}
			}
		}
		throw new ProcessManagerException("No such service. package="+pkg+" service="+service+" arguments="+Arrays.toString(arguments));
	}
	
	private ProcessInfo start(String packageName, String serviceName, String command, String[] arguments) throws ProcessManagerException {
		String[] cmd = command.trim().split(" ");
		String[] cmdAndArgs = new String[cmd.length+arguments.length];
		System.arraycopy(cmd, 0, cmdAndArgs, 0, cmd.length);
		System.arraycopy(arguments, 0, cmdAndArgs, cmd.length, arguments.length);
		String id = Long.toString(nextProcessId.getAndIncrement(), BASE_36);
		
		File dir = new File(runtimeDir, id);
		dir.mkdirs();
		File infoFile = new File(dir, INFO_FILENAME);
		File logFile = new File(dir, LOG_FILENAME);

		ProcessInfo info = new ProcessInfo()
				.setId(id)
				.setPackageName(packageName)
				.setServiceName(serviceName);
		
		try {
			FileUtil.writeFully(gson.toJson(info), infoFile);
			
			Process p = new ProcessBuilder(cmdAndArgs)
					.redirectOutput(logFile)
					.redirectError(logFile)
					.start();
			processes.put(id, p);
			
			return info;
		} catch(Throwable t) {
			throw new ProcessManagerException("Could not start process. cmd=" + Arrays.toString(cmdAndArgs) + " info=" + info, t);
		}
	}

	public boolean stop(String id, boolean forcibly) throws ProcessManagerException {
		Process p = processes.get(id);
		if(p == null) {
			throw new ProcessManagerException("No process with id " + id);
		}
		if(!p.isAlive()) {
			processes.remove(id);
			throw new ProcessManagerException("Process " + id + " was already stopped");
		}
		try {
			if(forcibly) {
				p.destroyForcibly();
			} else {
				p.destroy();
			}
			return true;
		} catch (Throwable t) {
			throw new ProcessManagerException("Could not stop Process id=" + id + " process=" + p);
		}
	}
	
	public String getLog(String id) throws ProcessManagerException {
		File dir = new File(runtimeDir, id);
		if(dir.isDirectory()) {
			File logFile = new File(dir, LOG_FILENAME);
			if(logFile.exists()) {
				try {
					return FileUtil.readFully(logFile);
				} catch (IOException e) {
					throw new ProcessManagerException("Could not read log for " + id, e);
				}
			}
		}
		throw new ProcessManagerException("No process with id " + id);
	}

	public List<ProcessInfo> getProcesses() {
		List<ProcessInfo> results = new ArrayList<>();
		for(File f : runtimeDir.listFiles(f -> f.isDirectory())) {
			try {
				ProcessInfo info = getProcessInfo(f.getName());
				if(info != null) {
					results.add(info);
				}
			} catch(ProcessManagerException e) {
				logger.error("Got error while iterating over process infos", e);
			}
		}
		return results;
	}
	
	private ProcessInfo getProcessInfo(String id) throws ProcessManagerException {
		File dir = new File(runtimeDir, id);
		if(dir.isDirectory()) {
			File infoFile = new File(dir, INFO_FILENAME);
			if(infoFile.exists()) {
				try {
					ProcessInfo info = gson.fromJson(FileUtil.readFully(infoFile), ProcessInfo.class);
					Process p = processes.get(id);
					boolean alive = p != null && p.isAlive();
					info.setAlive(alive);
					return info;
				} catch(Throwable t) {
					throw new ProcessManagerException("Error reading process information for " + dir.getAbsolutePath(), t);
				}
			}
		}
		throw new ProcessManagerException("No process with id " + id);
	}

	private void send(Session session, ProcessManagerResponse m) {
		try {
			session.getRemote().sendString(gson.toJson(m));
		} catch (IOException e) {
			if(logger.isDebugEnabled()) {
				logger.debug("Could not send response", e);
			}
		}
	}

}
