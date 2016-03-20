package rook.api.config;

import java.util.Map;

/**
 * Configuration used to instantiate a Service
 * 
 * @author Eric Thill
 *
 */
public class ServiceConfig {
	private String type;
	private String classpath;
	private Map<String, Object> config;
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	public void setClasspath(String classpath) {
		this.classpath = classpath;
	}
	
	public String getClasspath() {
		return classpath;
	}
	
	public Map<String, Object> getConfig() {
		return config;
	}
	
	public void setConfig(Map<String, Object> config) {
		this.config = config;
	}

	@Override
	public String toString() {
		return "ServiceConfig [type=" + type + ", classpath=" + classpath + ", config=" + config + "]";
	}

}
