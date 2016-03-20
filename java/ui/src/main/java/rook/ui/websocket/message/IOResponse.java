package rook.ui.websocket.message;

import java.util.ArrayList;
import java.util.List;

/**
 * Decoded message
 * 
 * @author Eric Thill
 *
 */
public class IOResponse {

	private String t;
	private List<Value> v;
	
	public IOResponse setType(String t) {
		this.t = t;
		return this;
	}
	
	public String getType() {
		return t;
	}
	
	public IOResponse setValues(List<Value> v) {
		this.v = v;
		return this;
	}
	
	public IOResponse addValue(Value v) {
		if(this.v == null) {
			this.v = new ArrayList<>();
		}
		this.v.add(v);
		return this;
	}
	
	public List<Value> getValues() {
		return v;
	}
	
	public static class Value {
		private String id;
		private String v;
		
		public String getId() {
			return id;
		}
		
		public Value setId(String id) {
			this.id = id;
			return this;
		}
		
		public String getValue() {
			return v;
		}
		
		public Value setValue(String v) {
			this.v = v;
			return this;
		}
	}
}
