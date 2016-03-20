package rook.api;

import java.util.LinkedHashMap;
import java.util.Map;

import rook.api.config.ServiceConfig;

/**
 * Rook Configuration. Easily deserialized from a JSON string using gson.
 * 
 * @author Eric Thill
 *
 */
public class RookConfig {
	private String description;
	private ServiceConfig router = null;
	private Map<String, ServiceConfig> services = new LinkedHashMap<>();

	public String getDescription() {
		return description;
	}
	
	public ServiceConfig getRouter() {
		return router;
	}

	public Map<String, ServiceConfig> getServices() {
		return services;
	}
}
