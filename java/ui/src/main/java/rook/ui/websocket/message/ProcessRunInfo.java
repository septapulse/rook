package rook.ui.websocket.message;

/**
 * Decoded Message
 * 
 * @author Eric Thill
 *
 */
public class ProcessRunInfo {
	private String pkg;
	private String name;
	private long uid;
	
	public String getPkg() {
		return pkg;
	}
	
	public ProcessRunInfo setPkg(String pkg) {
		this.pkg = pkg;
		return this;
	}
	
	public String getName() {
		return name;
	}
	
	public ProcessRunInfo setName(String name) {
		this.name = name;
		return this;
	}
	
	public long getUid() {
		return uid;
	}
	
	public ProcessRunInfo setUid(long uid) {
		this.uid = uid;
		return this;
	}
}
