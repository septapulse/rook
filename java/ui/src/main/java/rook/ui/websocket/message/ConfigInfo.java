package rook.ui.websocket.message;

/**
 * Decoded Message
 * 
 * @author Eric Thill
 *
 */
public class ConfigInfo {
	private String pkg;
	private String sid;
	private String configName;
	
	public String getPkg() {
		return pkg;
	}
	
	public ConfigInfo setPkg(String pkg) {
		this.pkg = pkg;
		return this;
	}
	
	public String getSid() {
		return sid;
	}
	
	public ConfigInfo setSid(String sid) {
		this.sid = sid;
		return this;
	}
	
	public String getConfigName() {
		return configName;
	}
	
	public ConfigInfo setConfigName(String configName) {
		this.configName = configName;
		return this;
	}
}
