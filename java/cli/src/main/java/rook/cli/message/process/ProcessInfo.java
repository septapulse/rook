package rook.cli.message.process;

import com.google.gson.Gson;

public class ProcessInfo {
	private String id;
	private String packageName;
	private String serviceName;
	private Boolean alive;

	public String getId() {
		return id;
	}
	
	public ProcessInfo setId(String id) {
		this.id = id;
		return this;
	}

	public String getPackageName() {
		return packageName;
	}
	
	public ProcessInfo setPackageName(String packageName) {
		this.packageName = packageName;
		return this;
	}
	
	public String getServiceName() {
		return serviceName;
	}
	
	public ProcessInfo setServiceName(String serviceName) {
		this.serviceName = serviceName;
		return this;
	}
	
	public Boolean getAlive() {
		return alive;
	}
	
	public ProcessInfo setAlive(Boolean alive) {
		this.alive = alive;
		return this;
	}
	
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
}