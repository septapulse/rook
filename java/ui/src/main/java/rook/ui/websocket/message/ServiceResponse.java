package rook.ui.websocket.message;

import java.util.List;

/**
 * Decoded message
 * 
 * @author Eric Thill
 *
 */
public class ServiceResponse {
	// TODO split request: 
	// TODO t=get_log -> get PID log @offset to offset+numLines, null offset means tail, must return offset in response
	// TODO t=get_config -> get existing group+serviceId+configName configuration json object
	// TODO t=get_config_template -> get group+serviceId configuration template
	// TODO t=put_config -> save new group+serviceId+configName configuration json object
		
	private Long id;
	private boolean success;
	private List<String> packages;
	private PackageSupportInfo packageInfo;
	private List<ConfigInfo> configs;
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getId() {
		return id;
	}
	
	public ServiceResponse setSuccess(boolean success) {
		this.success = success;
		return this;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public ServiceResponse setPackages(List<String> packages) {
		this.packages = packages;
		return this;
	}
	
	public List<String> getPackages() {
		return packages;
	}
	
	public ServiceResponse setPackageInfo(PackageSupportInfo packageInfo) {
		this.packageInfo = packageInfo;
		return this;
	}
	
	public PackageSupportInfo getPackageInfo() {
		return packageInfo;
	}
	
	public ServiceResponse setConfigs(List<ConfigInfo> configs) {
		this.configs = configs;
		return this;
	}
	
	public List<ConfigInfo> getConfigs() {
		return configs;
	}
	
}
