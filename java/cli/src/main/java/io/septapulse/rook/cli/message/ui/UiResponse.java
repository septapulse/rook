package io.septapulse.rook.cli.message.ui;

import java.util.Collection;

import com.google.gson.Gson;

import io.septapulse.rook.cli.message.Result;

public class UiResponse {

	private UiMessageType type;
	private Result result;
	private UiInfo ui;
	private Collection<UiInfo> uis;

	public UiMessageType getType() {
		return type;
	}

	public UiResponse setType(UiMessageType type) {
		this.type = type;
		return this;
	}
	
	public Result getResult() {
		return result;
	}
	
	public UiResponse setResult(Result result) {
		this.result = result;
		return this;
	}
	
	public UiInfo getUI() {
		return ui;
	}
	
	public UiResponse setUI(UiInfo ui) {
		this.ui = ui;
		return this;
	}

	public Collection<UiInfo> getUIs() {
		return uis;
	}
	
	public UiResponse setUIs(Collection<UiInfo> uis) {
		this.uis = uis;
		return this;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
