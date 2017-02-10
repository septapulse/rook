package run.rook.cli.message.pkg;

import java.util.List;

import com.google.gson.Gson;

public class ServiceInfo {
	private String id;
	private String name;
	private String command;
	private List<ArgumentInfo> arguments;

	public String getId() {
		return id;
	}

	public ServiceInfo setId(String id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public ServiceInfo setName(String name) {
		this.name = name;
		return this;
	}

	public String getCommand() {
		return command;
	}

	public ServiceInfo setCommand(String command) {
		this.command = command;
		return this;
	}

	public List<ArgumentInfo> getArguments() {
		return arguments;
	}

	public ServiceInfo setArguments(List<ArgumentInfo> arguments) {
		this.arguments = arguments;
		return this;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
}
