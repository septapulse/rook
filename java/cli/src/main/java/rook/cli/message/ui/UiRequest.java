package rook.cli.message.ui;

import com.google.gson.Gson;

public class UiRequest {

	private UiMessageType type;
	private String id;
	private String data;

	public UiMessageType getType() {
		return type;
	}

	public UiRequest setType(UiMessageType type) {
		this.type = type;
		return this;
	}

	public String getId() {
		return id;
	}

	public UiRequest setId(String id) {
		this.id = id;
		return this;
	}

	public String getData() {
		return data;
	}

	public UiRequest setData(String data) {
		this.data = data;
		return this;
	}
	
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
