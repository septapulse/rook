package run.rook.cli.message.pkg;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;

public class PackageInfo {
	private String id;
	private String name;
	private Map<String, ServiceInfo> services;
	
	public String getId() {
		return id;
	}
	
	public PackageInfo setId(String id) {
		this.id = id;
		return this;
	}
	
	public String getName() {
		return name;
	}
	
	public PackageInfo setName(String name) {
		this.name = name;
		return this;
	}
	
	public Map<String, ServiceInfo> getServices() {
		if(services == null) {
			return Collections.emptyMap();
		}
		return services;
	}
	
	public PackageInfo setServices(Map<String, ServiceInfo> services) {
		this.services = services;
		return this;
	}
	
	public PackageInfo addService(String id, ServiceInfo info) {
		if(services == null) {
			services = new LinkedHashMap<>();
		}
		services.put(id, info);
		return this;
	}
	
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
}