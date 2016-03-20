package rook.ui.websocket.message;

/**
 * Decoded message
 * 
 * @author Eric Thill
 *
 */
public class ScriptResponse {
	private boolean success;
	private Long id;
	private String code;
	private String error;
	
	public boolean isSuccess() {
		return success;
	}
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
	
	public String getError() {
		return error;
	}
	
	public void setError(String error) {
		this.error = error;
	}
}
