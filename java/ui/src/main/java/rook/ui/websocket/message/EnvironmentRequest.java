package rook.ui.websocket.message;

/**
 * Decoded message
 * 
 * @author Eric Thill
 *
 */
public class EnvironmentRequest {
	private String t;
	private String cfg;
	private boolean mqttBusEnabled = true;
	
	public String getType() {
		return t;
	}
	
	public String getCfg() {
		return cfg;
	}
	
	public boolean isMqttBusEnabled() {
		return mqttBusEnabled;
	}
}
