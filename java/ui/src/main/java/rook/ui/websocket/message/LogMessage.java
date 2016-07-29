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
	private long uid;
	
	public String getType() {
		return t;
	}
	
	public LogMessage setMessage(String m) {
		this.m = m;
		return this;
	}
	
	public String getMessage() {
		return m;
	}
	
	public long getUid() {
		return uid;
	}
	
	public LogMessage setUid(long uid) {
		this.uid = uid;
		return this;
	}
}
