package io.septapulse.rook.cli.message.pkg;

import java.util.Map;

import com.google.gson.Gson;

public class ServiceInfo {
	private String id;
	private String name;
	private String command;
	private Map<String, ArgumentInfo> arguments;

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

	public Map<String, ArgumentInfo> getArguments() {
		return arguments;
	}

	public ServiceInfo setArguments(Map<String, ArgumentInfo> arguments) {
		this.arguments = arguments;
		return this;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
}
