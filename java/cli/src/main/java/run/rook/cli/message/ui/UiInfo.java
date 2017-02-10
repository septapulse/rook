package run.rook.cli.message.ui;

import com.google.gson.Gson;

public class UiInfo  {
	private String id;
	private String name;
	private String image;
	
	public String getId() {
		return id;
	}
	
	public UiInfo setId(String id) {
		this.id = id;
		return this;
	}
	
	public String getName() {
		return name;
	}
	
	public UiInfo setName(String name) {
		this.name = name;
		return this;
	}
	
	public String getImage() {
		return image;
	}
	
	public UiInfo setImage(String image) {
		this.image = image;
		return this;
	}
	
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
}