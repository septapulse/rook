package rook.daemon.packages;

import java.util.Collection;

import com.google.gson.Gson;

import rook.daemon.common.Result;

public class PackageManagerResponse {

	private MessageType type;
	private Result result;
	private PackageInfo pkg;
	private Collection<PackageInfo> packages;

	public MessageType getType() {
		return type;
	}

	public PackageManagerResponse setType(MessageType type) {
		this.type = type;
		return this;
	}
	
	public Result getResult() {
		return result;
	}
	
	public PackageManagerResponse setResult(Result result) {
		this.result = result;
		return this;
	}
	
	public PackageInfo getPackage() {
		return pkg;
	}
	
	public PackageManagerResponse setPackage(PackageInfo pkg) {
		this.pkg = pkg;
		return this;
	}

	public Collection<PackageInfo> getPackages() {
		return packages;
	}
	
	public PackageManagerResponse setPackages(Collection<PackageInfo> packages) {
		this.packages = packages;
		return this;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
