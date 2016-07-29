package rook.ui.websocket.message;

/**
 * Decoded message
 * 
 * @author Eric Thill
 *
 */
public class RuntimeRequest {
	private String t;
	private Long id;
	private String pkg;
	private String sid;
	private String cfg;
	private Long uid;
	
	public String getType() {
		return t;
	}
	
	public Long getId() {
		return id;
	}
	
	public String getPkg() {
		return pkg;
	}

	public String getSid() {
		return sid;
	}
	
	public String getCfg() {
		return cfg;
	}
	
	public Long getUid() {
		return uid;
	}
}
