package rook.cli.message.pkg;

import com.google.gson.Gson;

public class PackageRequest {

	private PackageMessageType type;
	private String id;
	private String data;

	public PackageMessageType getType() {
		return type;
	}

	public PackageRequest setType(PackageMessageType type) {
		this.type = type;
		return this;
	}

	public String getId() {
		return id;
	}

	public PackageRequest setId(String id) {
		this.id = id;
		return this;
	}

	public String getData() {
		return data;
	}

	public PackageRequest setData(String data) {
		this.data = data;
		return this;
	}
	
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
