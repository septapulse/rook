package rook.ui.websocket.message;

import java.util.List;

/**
 * Decoded message
 * 
 * @author Eric Thill
 *
 */
public class ConfigResponse {
	private Long id;
	private boolean success;
	private List<ConfigInfo> cfgs;
//	private Map<String, Object> cfg;
//	private String template;
	
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
	
	public ConfigResponse setCfgs(List<ConfigInfo> cfgs) {
		this.cfgs = cfgs;
		return this;
	}
	
	public List<ConfigInfo> getCfgs() {
		return cfgs;
	}
	
//	public void setCfg(Map<String, Object> cfg) {
//		this.cfg = cfg;
//	}
//	
//	public Map<String, Object> getCfg() {
//		return cfg;
//	}
//	
//	public void setTemplate(String template) {
//		this.template = template;
//	}
//	
//	public String getTemplate() {
//		return template;
//	}
}
