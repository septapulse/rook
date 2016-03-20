package rook.ui.environment;

import java.io.File;
import java.io.IOException;

import rook.api.InitException;
import rook.api.RID;
import rook.api.RookConfig;
import rook.api.RookRunner;
import rook.api.Router;
import rook.core.io.proxy.IOProxy;

/**
 * Utility to start, stop, and hook into a Rook environment
 * 
 * @author Eric Thill
 *
 */
public class Environment {

	private static final String ENVIRONMENT_BUS_CONFIG = "cfg/ui/environment.json";
	
	private final Log log = new Log();
	private final File configDir;
	private final File serviceDir;
	private final Controller controller;
	private final ConfigManager configManager;
	private final Script script;
	
	private final Router router;
	private final UIService uiService;
	
	public Environment(File directory, RookConfig proxyConfig) throws InitException {
		configDir = new File(directory, "cfg/env");
		serviceDir = new File(directory, "services");
		controller = new Controller(directory, log::dispatch);
		configManager = new ConfigManager(configDir, serviceDir);
		
		RookRunner rookRunner = new RookRunner();
		router = rookRunner.instantiate(proxyConfig);
		uiService = new UIService();
		router.addService(RID.create("UI"), uiService);
		router.start();
		
		waitForStartup();
		script = new Script(uiService.getIOProxy());
	}
	
	private void waitForStartup() throws InitException {
		long start = System.currentTimeMillis();
		while(uiService.getIOProxy() == null && System.currentTimeMillis()-start < 2000) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(uiService.getIOProxy() == null) {
			throw new InitException("UI Service was not initialized");
		}
	}

	public void start(String config) throws IOException {
		stop();
		uiService.getIOProxy().reset();
		log.clear();
		controller.start(ENVIRONMENT_BUS_CONFIG, new File(configDir, config).getAbsolutePath());
		new Thread(this::delayRequestCaps).start();
	}
	
	private void delayRequestCaps() {
		try {
			// TODO configurable sleep time
			Thread.sleep(3000);
		} catch (InterruptedException e) { }
		uiService.getIOProxy().caps().requestCaps();
	}
	
	public void stop() {
		controller.stop();
	}
	
	public IOProxy ioProxy() {
		return uiService.getIOProxy();
	}
	
	public ConfigManager configManager() {
		return configManager;
	}
	
	public Log log() {
		return log;
	}
	
	public Script script() {
		return script;
	}
	
}
