package rook.ui.websocket.message;

import java.util.Map;

/**
 * Decoded message
 * 
 * @author Eric Thill
 *
 */
public class ConfigRequest {
	private String t;
	private Long id;
	private String name;
	private String library;
	private Map<String, Object> cfg;
	
	public String getType() {
		return t;
	}
	
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public String getLibrary() {
		return library;
	}
	
	public Map<String, Object> getCfg() {
		return cfg;
	}
}
