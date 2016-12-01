package rook.daemon.packages;

import com.google.gson.Gson;

public class PackageManagerRequest {

	private MessageType type;
	private String id;
	private String data;

	public MessageType getType() {
		return type;
	}

	public PackageManagerRequest setType(MessageType type) {
		this.type = type;
		return this;
	}

	public String getId() {
		return id;
	}

	public PackageManagerRequest setId(String id) {
		this.id = id;
		return this;
	}

	public String getData() {
		return data;
	}

	public PackageManagerRequest setData(String data) {
		this.data = data;
		return this;
	}
	
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
