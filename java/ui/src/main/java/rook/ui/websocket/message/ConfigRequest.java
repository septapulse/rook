package rook.ui.websocket.message;

/**
 * Decoded message
 * 
 * @author Eric Thill
 *
 */
public class ConfigRequest {
	private String t;
	private Long id;
	private String pkg;
	private String sid;
//	private String name;
//	private String library;
//	private Map<String, Object> cfg;
	
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
}
