package rook.cli.message.pkg;

import java.util.Collection;

import com.google.gson.Gson;

import rook.cli.message.Result;

public class PackageResponse {

	private PackageMessageType type;
	private Result result;
	private PackageInfo pkg;
	private Collection<PackageInfo> packages;

	public PackageMessageType getType() {
		return type;
	}

	public PackageResponse setType(PackageMessageType type) {
		this.type = type;
		return this;
	}
	
	public Result getResult() {
		return result;
	}
	
	public PackageResponse setResult(Result result) {
		this.result = result;
		return this;
	}
	
	public PackageInfo getPackage() {
		return pkg;
	}
	
	public PackageResponse setPackage(PackageInfo pkg) {
		this.pkg = pkg;
		return this;
	}

	public Collection<PackageInfo> getPackages() {
		return packages;
	}
	
	public PackageResponse setPackages(Collection<PackageInfo> packages) {
		this.packages = packages;
		return this;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}

}
