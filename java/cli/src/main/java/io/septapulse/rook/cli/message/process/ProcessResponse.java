package io.septapulse.rook.cli.message.process;

import java.util.Collection;

import com.google.gson.Gson;

import io.septapulse.rook.cli.message.Result;

public class ProcessResponse {

	private ProcessMessageType type;
	private Result result;
	private ProcessInfo process;
	private Collection<ProcessInfo> processes;
	private String log;

	public ProcessMessageType getType() {
		return type;
	}

	public ProcessResponse setType(ProcessMessageType type) {
		this.type = type;
		return this;
	}
	
	public Result getResult() {
		return result;
	}
	
	public ProcessResponse setResult(Result result) {
		this.result = result;
		return this;
	}
	
	public ProcessInfo getProcess() {
		return process;
	}
	
	public ProcessResponse setProcess(ProcessInfo process) {
		this.process = process;
		return this;
	}

	public Collection<ProcessInfo> getProcesses() {
		return processes;
	}
	
	public ProcessResponse setProcesses(Collection<ProcessInfo> processes) {
		this.processes = processes;
		return this;
	}
	
	public String getLog() {
		return log;
	}
	
	public ProcessResponse setLog(String log) {
		this.log = log;
		return this;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
