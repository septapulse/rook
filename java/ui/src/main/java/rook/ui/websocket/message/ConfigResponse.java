package rook.ui.websocket.message;

import java.util.List;
import java.util.Map;

import rook.ui.environment.ServiceInfo;

/**
 * Decoded message
 * 
 * @author Eric Thill
 *
 */
public class ConfigResponse {
	private Long id;
	private boolean success;
	private List<String> cfgs;
	private Map<String, Object> cfg;
	private String template;
	private List<ServiceInfo> services;
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public void setCfgs(List<String> cfgs) {
		this.cfgs = cfgs;
	}
	
	public List<String> getCfgs() {
		return cfgs;
	}
	
	public void setCfg(Map<String, Object> cfg) {
		this.cfg = cfg;
	}
	
	public Map<String, Object> getCfg() {
		return cfg;
	}
	
	public void setTemplate(String template) {
		this.template = template;
	}
	
	public String getTemplate() {
		return template;
	}
	
	public void setServices(List<ServiceInfo> services) {
		this.services = services;
	}
	
	public List<ServiceInfo> getServices() {
		return services;
	}
}
