package rook.ui.websocket.message;

/**
 * Decoded message
 * 
 * @author Eric Thill
 *
 */
public class LogMessage {
	private String t = "log";
	private String m;
	
	public String getType() {
		return t;
	}
	
	public void setMessage(String m) {
		this.m = m;
	}
	
	public String getMessage() {
		return m;
	}
}
