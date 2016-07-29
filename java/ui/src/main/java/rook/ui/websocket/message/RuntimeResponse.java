package rook.ui.websocket.message;

import java.util.List;

/**
 * Decoded message
 * 
 * @author Eric Thill
 *
 */
public class RuntimeResponse {
	// TODO split request: 
	// TODO XXX t=get_group -> get group supported services, and each supported service's existing config names
	// TODO XXX t=start -> start group+serviceId+configName and return PID
	// TODO XXX t=stop -> stop PID
	// TODO t=get_log -> get PID log @offset to offset+numLines, null offset means tail, must return offset in response
	// TODO t=get_config -> get existing group+serviceId+configName configuration json object
	// TODO t=get_config_template -> get group+serviceId configuration template
	// TODO t=put_config -> save new group+serviceId+configName configuration json object
		
	private Long id;
	private boolean success;
	private List<ProcessRunInfo> running;
	private ProcessRunInfo instance;
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Long getId() {
		return id;
	}
	
	public RuntimeResponse setSuccess(boolean success) {
		this.success = success;
		return this;
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public RuntimeResponse setInstance(ProcessRunInfo instance) {
		this.instance = instance;
		return this;
	}
	
	public ProcessRunInfo getInstance() {
		return instance;
	}
	
	public RuntimeResponse setRunning(List<ProcessRunInfo> running) {
		this.running = running;
		return this;
	}
	
	public List<ProcessRunInfo> getRunning() {
		return running;
	}
	
}
