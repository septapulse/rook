package rook.ui.websocket.message;

/**
 * Decoded message
 * 
 * @author Eric Thill
 *
 */
public class ScriptRequest {
	private String t;
	private Long id;
	private String code;
	
	public String getType() {
		return t;
	}
	
	public Long getId() {
		return id;
	}
	
	public String getCode() {
		return code;
	}
	
}
