package io.septapulse.rook.cli.message;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.google.gson.Gson;

public class Result {
	private Boolean success;
	private String error;
	
	public Boolean getSuccess() {
		return success;
	}
	
	public Result setSuccess(Boolean success) {
		this.success = success;
		return this;
	}
	
	public String getError() {
		return error;
	}
	
	public Result setError(String error) {
		this.error = error;
		return this;
	}
	
	public Result setError(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		error = sw.toString();
		return this;
	}
	
	public Result setError(String error, Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		error = error + "\n" + sw.toString();
		return this;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
}
