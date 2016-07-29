package rook.ui.websocket.message;

/**
 * Decoded message
 * 
 * @author Eric Thill
 *
 */
public class ServiceRequest {
	private String t;
	private Long id;
	private String pkg;
	
	public String getType() {
		return t;
	}
	
	public Long getId() {
		return id;
	}
	
	public String getPkg() {
		return pkg;
	}

}
