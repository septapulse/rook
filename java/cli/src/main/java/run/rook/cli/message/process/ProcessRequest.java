package run.rook.cli.message.process;

import com.google.gson.Gson;

public class ProcessRequest {

	private ProcessMessageType type;
	private String id;
	private String pkg;
	private String service;
	private String[] arguments;
	
	public ProcessMessageType getType() {
		return type;
	}

	public ProcessRequest setType(ProcessMessageType type) {
		this.type = type;
		return this;
	}
	
	public String getId() {
		return id;
	}
	
	public ProcessRequest setId(String id) {
		this.id = id;
		return this;
	}

	public String getPackage() {
		return pkg;
	}
	
	public ProcessRequest setPackage(String pkg) {
		this.pkg = pkg;
		return this;
	}
	
	public String getService() {
		return service;
	}
	
	public ProcessRequest setService(String service) {
		this.service = service;
		return this;
	}
	
	public String[] getArguments() {
		return arguments;
	}
	
	public ProcessRequest setArguments(String[] arguments) {
		this.arguments = arguments;
		return this;
	}
	
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
