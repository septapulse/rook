package rook.daemon.processes;

import com.google.gson.Gson;

public class ProcessManagerRequest {

	private MessageType type;
	private String id;
	private String pkg;
	private String service;
	private String[] arguments;
	
	public MessageType getType() {
		return type;
	}

	public ProcessManagerRequest setType(MessageType type) {
		this.type = type;
		return this;
	}
	
	public String getId() {
		return id;
	}
	
	public ProcessManagerRequest setId(String id) {
		this.id = id;
		return this;
	}

	public String getPackage() {
		return pkg;
	}
	
	public ProcessManagerRequest setPackage(String pkg) {
		this.pkg = pkg;
		return this;
	}
	
	public String getService() {
		return service;
	}
	
	public ProcessManagerRequest setService(String service) {
		this.service = service;
		return this;
	}
	
	public String[] getArguments() {
		return arguments;
	}
	
	public ProcessManagerRequest setArguments(String[] arguments) {
		this.arguments = arguments;
		return this;
	}
	
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
