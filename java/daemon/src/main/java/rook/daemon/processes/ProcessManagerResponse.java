package rook.daemon.processes;

import java.util.Collection;

import com.google.gson.Gson;

import rook.daemon.common.Result;

public class ProcessManagerResponse {

	private MessageType type;
	private Result result;
	private ProcessInfo process;
	private Collection<ProcessInfo> processes;
	private String log;

	public MessageType getType() {
		return type;
	}

	public ProcessManagerResponse setType(MessageType type) {
		this.type = type;
		return this;
	}
	
	public Result getResult() {
		return result;
	}
	
	public ProcessManagerResponse setResult(Result result) {
		this.result = result;
		return this;
	}
	
	public ProcessInfo getProcess() {
		return process;
	}
	
	public ProcessManagerResponse setProcess(ProcessInfo process) {
		this.process = process;
		return this;
	}

	public Collection<ProcessInfo> getProcesses() {
		return processes;
	}
	
	public ProcessManagerResponse setProcesses(Collection<ProcessInfo> processes) {
		this.processes = processes;
		return this;
	}
	
	public String getLog() {
		return log;
	}
	
	public ProcessManagerResponse setLog(String log) {
		this.log = log;
		return this;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
