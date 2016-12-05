package io.septapulse.rook.daemon.websocket;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Collection;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.septapulse.rook.cli.message.Result;
import io.septapulse.rook.cli.message.pkg.PackageInfo;
import io.septapulse.rook.cli.message.pkg.PackageMessageType;
import io.septapulse.rook.cli.message.pkg.PackageRequest;
import io.septapulse.rook.cli.message.pkg.PackageResponse;

@WebSocket
public class PackageManager {
	
	public static final String PACKAGE_MANAGER_PROTOCOL = "PACKAGE";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Gson gson = new Gson();
	private final ZipManager<PackageInfo> zipManager;
	
	public PackageManager(File platformDir, File usrDir) {
		platformDir.mkdirs();
		usrDir.mkdirs();
		zipManager = new ZipManager<>(PackageInfo.class, 
				this::getId, this::setId, this::getName,
				new File[] {platformDir, usrDir}, usrDir);
	}
	
	private String getId(PackageInfo o) {
		return o.getId();
	}
	
	private void setId(PackageInfo o, String id) {
		o.setId(id);
	}
	
	private String getName(PackageInfo o) {
		return o.getName();
	}
	
	public void init() {
		zipManager.refresh();
	}
	
	@OnWebSocketMessage
	public void onText(Session session, String message) throws IOException {
		PackageRequest req = gson.fromJson(message, PackageRequest.class);
		Result result;
		try {
			switch (req.getType()) {
			case LIST:
				Collection<PackageInfo> packages = zipManager.all();
				// no service info when all packages returned at once
				for(PackageInfo info : packages) {
					info.setServices(null);
				}
				result = new Result().setSuccess(packages != null);
				send(session, new PackageResponse()
						.setType(PackageMessageType.LIST)
						.setResult(result)
						.setPackages(packages));
				break;
			case GET:
				PackageInfo pkg = zipManager.get(req.getId());
				result = new Result().setSuccess(pkg != null);
				if(pkg == null) {
					result.setError("Package '" + req.getId() + "' does not exist");
				}
				send(session, new PackageResponse()
						.setType(PackageMessageType.LIST)
						.setResult(result)
						.setPackage(pkg));
				break;
			case ADD:
				zipManager.add(Base64.getDecoder().decode(req.getData()));
				result = new Result().setSuccess(true);
				send(session, new PackageResponse()
						.setType(PackageMessageType.ADD)
						.setResult(result));
				break;
			case REMOVE:
				boolean success = zipManager.remove(req.getId());
				result = new Result().setSuccess(success);
				if(!success) {
					result.setError("No package with id=" + req.getId());
				}
				send(session, new PackageResponse()
						.setType(PackageMessageType.REMOVE)
						.setResult(result));
				break;
			case REFRESH:
				zipManager.refresh();
				result = new Result().setSuccess(true);
				send(session, new PackageResponse()
						.setType(PackageMessageType.REFRESH)
						.setResult(result));
				break;
				
			}
		} catch(Throwable t) {
			logger.error("PackageManager failed to process " + message);
			result = new Result().setSuccess(false).setError(t);
			send(session, new PackageResponse()
					.setType(PackageMessageType.REFRESH)
					.setResult(result));
		}
	}

	private void send(Session session, PackageResponse m) {
		try {
			session.getRemote().sendString(gson.toJson(m));
		} catch (IOException e) {
			if(logger.isDebugEnabled()) {
				logger.debug("Could not send response", e);
			}
		}
	}

//	public Result addPackage(byte[] data) {
//		try {
//			String randomUUID = UUID.randomUUID().toString(); 
//			File dest = new File(usrDir, randomUUID+ZIP_SUFFIX);
//			FileOutputStream fos = new FileOutputStream(dest);
//			fos.write(data);
//			fos.close();
//			refresh();
//			return new Result().setSuccess(true);
//		} catch(IOException e) {
//			logger.warn("Could not add new package", e);
//			return new Result()
//					.setSuccess(false)
//					.setError("Could not add package", e);
//		}
//	}
//
//	public Result removePackage(String id) {
//		File dir = new File(usrDir, id);
//		File md5 = new File(usrDir, id+MD5_SUFFIX);
//		File zip = new File(usrDir, id+ZIP_SUFFIX);
//		if(FileUtil.delete(md5) | FileUtil.delete(dir) | FileUtil.delete(zip)) {
//			refresh();
//			return new Result().setSuccess(true);
//		} else {
//			return new Result()
//					.setSuccess(false)
//					.setError("Package '" + id + " does not exist");
//		}
//	}
//	
//	public PackageInfo getPackage(String id) {
//		PackageInfo pkg = getPackage(id, platformDir);
//		if(pkg == null) {
//			pkg = getPackage(id, usrDir);
//		}
//		return pkg;
//	}
//	
//	private PackageInfo getPackage(String id, File dir) {
//		File pkgDir = new File(dir, id);
//		if(pkgDir.isDirectory()) {
//			File cfg = new File(pkgDir, ROOK_CFG_FILENAME);
//			if(cfg.exists()) {
//				try {
//					PackageInfo info = gson
//							.fromJson(FileUtil.readFully(cfg), PackageInfo.class)
//							// override ID with actual directory ID
//							.setId(id);
//					return info;
//				} catch(IOException e) {
//					logger.warn("Could not read package " + dir.getAbsolutePath(), e);
//				}
//			}
//		}
//		return null;
//	}
//
//	public Collection<PackageInfo> getPackages() {
//		List<PackageInfo> packages = new ArrayList<>();
//		packages.addAll(getPackages(platformDir));
//		packages.addAll(getPackages(usrDir));
//		return packages;
//	}
//	
//	private List<PackageInfo> getPackages(File dir) {
//		List<PackageInfo> packages = new ArrayList<>();
//		for(File f : dir.listFiles(t -> t.isDirectory())) {
//			String id = f.getName();
//			File cfg = new File(f, ROOK_CFG_FILENAME);
//			if(cfg.exists()) {
//				try {
//					PackageInfo info = gson
//							.fromJson(FileUtil.readFully(cfg), PackageInfo.class)
//							// override ID with actual directory ID
//							.setId(id)
//							// services should only exist when a single package info is requested
//							.setServices(null);
//					packages.add(info);
//				} catch(IOException e) {
//					logger.error("Could not read package " + dir.getAbsolutePath(), e);
//				}
//			}
//		}
//		packages.sort(this::compareName);
//		return packages;
//	}
//	
//	private int compareName(PackageInfo p1, PackageInfo p2) {
//		return p1.getName().toLowerCase().compareTo(p2.getName().toLowerCase());
//	}
//
//	public void refresh() {
//		refresh(platformDir);
//		refresh(usrDir);
//	}
//	
//	private void refresh(File dir) {
//		for(File f : dir.listFiles(f -> f.getName().endsWith(ZIP_SUFFIX))) {
//			try {
//				String filename = f.getName();
//				String md5new = GrowableBuffer.copyFrom(md5digest.digest(FileUtil.readFully(f).getBytes())).toHex();
//				File unzippedDir = new File(filename.substring(0, filename.length()-ZIP_SUFFIX.length()));
//				File infoFile = new File(unzippedDir, ROOK_CFG_FILENAME);
//				if(!infoFile.exists()) {
//					// no matching directory of zip name: new package to process
//					logger.info("Found new zip '" + filename + "'");
//					// unzip package
//					FileUtil.delete(unzippedDir);
//					unzip(f, unzippedDir);
//					if(!infoFile.exists()) {
//						logger.error(f.getAbsolutePath() + " does not contain " + ROOK_CFG_FILENAME);
//						break;
//					}
//					// read package info to get proper ID
//					PackageInfo pi = gson.fromJson(FileUtil.readFully(infoFile), PackageInfo.class);
//					String id = pi.getId();
//					// check if a directory with this id already exists (use case: new version uploaded)
//					File destDir = new File(dir, id);
//					if(destDir.exists()) {
//						logger.info("Updating package '" + id + "'");
//						FileUtil.delete(unzippedDir);
//					} else {
//						logger.info("Adding new package '" + id + "'");
//					}
//					// rename zip and directory to match id
//					f.renameTo(new File(dir, id+ZIP_SUFFIX));
//					unzippedDir.renameTo(new File(dir, id));
//					// write md5 file
//					File md5file = new File(dir, id+MD5_SUFFIX);
//					FileUtil.writeFully(md5new, md5file);
//				} else {
//					// matching directory of zip name exists: existing package
//					String id = unzippedDir.getName();
//					File md5file = new File(dir, id+MD5_SUFFIX);
//					if(!md5file.exists() || !md5new.equals(FileUtil.readFully(md5file))) {
//						logger.info("Updating package '" + id + "'");
//						// delete existing
//						FileUtil.delete(unzippedDir);
//						// unzip new
//						unzip(f, unzippedDir);
//						// write updated md5 file
//						FileUtil.writeFully(md5new, md5file);
//					}
//				}
//			} catch(IOException e) {
//				logger.error("Could not process " + f.getAbsolutePath(), e);
//			}
//		}
//	}
//	
//	private void unzip(File source, File destination) throws IOException {
//		try {
//			ZipFile zipFile = new ZipFile(source);
//	        zipFile.extractAll(destination.getAbsolutePath());
//		} catch(ZipException e) {
//			throw new IOException("Could not unzip " + source.getAbsolutePath(), e);
//		}
//	}
	

}
