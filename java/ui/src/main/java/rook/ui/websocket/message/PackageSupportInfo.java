package rook.ui.websocket.message;

import java.util.List;

/**
 * Decoded Message
 * 
 * @author Eric Thill
 *
 */
public class PackageSupportInfo {
	private String pkg;
	private List<ServiceSupportInfo> services;
	
	public String getPkg() {
		return pkg;
	}
	
	public PackageSupportInfo setPkg(String pkg) {
		this.pkg = pkg;
		return this;
	}
	
	public List<ServiceSupportInfo> getServices() {
		return services;
	}
	
	public PackageSupportInfo setServices(List<ServiceSupportInfo> services) {
		this.services = services;
		return this;
	}
}
