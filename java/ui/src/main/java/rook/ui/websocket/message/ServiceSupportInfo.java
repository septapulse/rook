package rook.ui.websocket.message;

/**
 * Decoded Message
 * 
 * @author Eric Thill
 *
 */
public class ServiceSupportInfo {
	private String pkg;
	private String id;
	private String name;
	
	public String getPkg() {
		return pkg;
	}
	
	public ServiceSupportInfo setPkg(String pkg) {
		this.pkg = pkg;
		return this;
	}
	
	public String getId() {
		return id;
	}
	
	public ServiceSupportInfo setId(String id) {
		this.id = id;
		return this;
	}
	
	public String getName() {
		return name;
	}
	
	public ServiceSupportInfo setName(String name) {
		this.name = name;
		return this;
	}
	
}
