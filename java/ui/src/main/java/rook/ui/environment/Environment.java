package rook.ui.environment;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.exception.InitException;
import io.septapulse.rook.api.reflect.Instantiate;
import io.septapulse.rook.api.transport.ControllableTransport;
import io.septapulse.rook.core.io.proxy.IOProxy;

/**
 * Utility to start, stop, and hook into a Rook environment
 * 
 * @author Eric Thill
 *
 */
@Deprecated
public class Environment {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final File apiDir;
	private final File[] serviceDirs;
	private final File[] cfgDirs;
	private final Class<?> routerType; 
	private final String routerConfig; 
	private final String routerPkg; 
	private final Class<?> transportType; 
	private final String transportConfig;
	private final File runDir; 
	private final File jreDir;
	private ControllableTransport rookTransport;
	private IOProxy ioProxy;
	private ServiceManager serviceManager;
	private ConfigManager configManager;
	private RuntimeManager runtimeManager;
	
	public Environment(Class<?> routerType, String routerConfig, String routerPkg, 
			Class<?> transportType, String transportConfig, 
			File platformDir, File userDir, File runDir, File jreDir) throws InitException, IOException {
		apiDir = new File(platformDir, "api");
		serviceDirs = new File[] { 
				new File(userDir,"services"),
				new File(platformDir,"services") 
		};
		cfgDirs = new File[] { 
				new File(userDir,"cfg"),
				new File(platformDir,"cfg")
		};
		this.routerType = routerType;
		this.routerConfig = routerConfig;
		this.routerPkg = routerPkg;
		this.transportType = transportType;
		this.transportConfig = transportConfig;
		this.runDir = runDir;
		this.jreDir = jreDir;
	}
	
	public void start() throws InitException {
		try {
			this.serviceManager = new ServiceManager(serviceDirs);
			this.configManager = new ConfigManager(cfgDirs, serviceDirs);
			this.runtimeManager = new RuntimeManager(transportType, transportConfig,
					jreDir, apiDir, runDir, serviceDirs, cfgDirs);
			
			if(routerType != null) {
				logger.info("Starting " + routerType + " config=" + routerConfig);
				runtimeManager.startRouter(routerPkg, routerType.getName(), routerConfig);
				logger.info("Giving Router 3 seconds to spin up");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
	
				}
			}
			
			logger.info("Creating " + transportType.getName() + " config=" + transportConfig);
			rookTransport = Instantiate.instantiate(transportType, transportConfig);
			rookTransport.setServiceId(RID.create("UI"));
			rookTransport.start();
			
			ioProxy = new IOProxy(rookTransport);
			refreshCache();
		} catch(InitException e) {
			throw e;
		} catch(Throwable t) {
			throw new InitException("Could not start Environment", t);
		}
	}
	
	public void refreshCache() {
		ioProxy.caps().requestCaps();
		rookTransport.announce().probe();
	}
	
	public IOProxy getIoProxy() {
		return ioProxy;
	}
	
	public ServiceManager getServiceManager() {
		return serviceManager;
	}
	
	public ConfigManager getConfigManager() {
		return configManager;
	}
	
	public RuntimeManager getRuntimeManager() {
		return runtimeManager;
	}
}
