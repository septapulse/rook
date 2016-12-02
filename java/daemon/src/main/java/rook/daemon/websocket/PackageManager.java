package rook.daemon.websocket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import rook.api.transport.GrowableBuffer;
import rook.api.util.FileUtil;
import rook.cli.message.Result;
import rook.cli.message.pkg.PackageMessageType;
import rook.cli.message.pkg.PackageInfo;
import rook.cli.message.pkg.PackageRequest;
import rook.cli.message.pkg.PackageResponse;

@WebSocket
public class PackageManager {
	
	public static final String PACKAGE_MANAGER_PROTOCOL = "PACKAGE";
	public static final String ROOK_CFG_FILENAME = "rook.cfg";
	
	private static final String ZIP_SUFFIX = ".zip";
	private static final String MD5_SUFFIX = ".md5";
	private final MessageDigest md5digest;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Gson gson = new Gson();
	private final File platformDir;
	private final File usrDir;
	
	public PackageManager(File platformDir, File usrDir) {
		platformDir.mkdirs();
		usrDir.mkdirs();
		try {
			md5digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new InstantiationError("Could not create MD5 digest");
		}
		this.platformDir = platformDir;
		this.usrDir = usrDir;
	}
	
	@OnWebSocketMessage
	public void onText(Session session, String message) throws IOException {
		PackageRequest req = gson.fromJson(message, PackageRequest.class);
		Result result;
		switch (req.getType()) {
		case LIST:
			Collection<PackageInfo> packages = getPackages();
			result = new Result().setSuccess(packages != null);
			send(session, new PackageResponse()
					.setType(PackageMessageType.LIST)
					.setResult(result)
					.setPackages(packages));
			break;
		case GET:
			PackageInfo pkg = getPackage(req.getId());
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
			result = addPackage(req.getId(), 
					Base64.getDecoder().decode(req.getData()));
			send(session, new PackageResponse()
					.setType(PackageMessageType.ADD)
					.setResult(result));
			break;
		case REMOVE:
			result = removePackage(req.getId());
			send(session, new PackageResponse()
					.setType(PackageMessageType.REFRESH)
					.setResult(result));
			break;
		case REFRESH:
			refresh();
			result = new Result().setSuccess(true);
			send(session, new PackageResponse()
					.setType(PackageMessageType.REFRESH)
					.setResult(result));
			break;
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

	public Result addPackage(String id, byte[] data) {
		try {
			removePackage(id);
			File dest = new File(usrDir, id+ZIP_SUFFIX);
			FileOutputStream fos = new FileOutputStream(dest);
			fos.write(data);
			fos.close();
			refresh();
			return new Result().setSuccess(true);
		} catch(IOException e) {
			logger.warn("Could not log package " + id, e);
			return new Result()
					.setSuccess(false)
					.setError("Could not add package " + id, e);
		}
	}

	public Result removePackage(String id) {
		File dir = new File(usrDir, id);
		File md5 = new File(usrDir, id+MD5_SUFFIX);
		File zip = new File(usrDir, id+ZIP_SUFFIX);
		if(FileUtil.delete(md5) | FileUtil.delete(dir) | FileUtil.delete(zip)) {
			refresh();
			return new Result().setSuccess(true);
		} else {
			return new Result()
					.setSuccess(false)
					.setError("Package '" + id + " does not exist");
		}
	}
	
	public PackageInfo getPackage(String id) {
		PackageInfo pkg = getPackage(id, platformDir);
		if(pkg == null) {
			pkg = getPackage(id, usrDir);
		}
		return pkg;
	}
	
	private PackageInfo getPackage(String id, File dir) {
		File pkgDir = new File(dir, id);
		if(pkgDir.isDirectory()) {
			File cfg = new File(pkgDir, ROOK_CFG_FILENAME);
			if(cfg.exists()) {
				try {
					PackageInfo info = gson
							.fromJson(FileUtil.readFully(cfg), PackageInfo.class)
							.setId(id);
					return info;
				} catch(IOException e) {
					logger.warn("Could not read package " + dir.getAbsolutePath(), e);
				}
			}
		}
		return null;
	}

	public Collection<PackageInfo> getPackages() {
		Map<String, PackageInfo> packages = new LinkedHashMap<>();
		getPackages(platformDir, packages);
		getPackages(usrDir, packages);
		return packages.values();
	}
	
	private void getPackages(File dir, Map<String, PackageInfo> dest) {
		for(File f : dir.listFiles(t -> t.isDirectory())) {
			String id = f.getName();
			if(!dest.containsKey(id)) {
				File cfg = new File(f, ROOK_CFG_FILENAME);
				if(cfg.exists()) {
					try {
						// override ID with actual directory ID
						// services should only exist when a single package info is requested
						PackageInfo info = gson
								.fromJson(FileUtil.readFully(cfg), PackageInfo.class)
								.setId(id)
								.setServices(null);
						dest.put(id, info);
					} catch(IOException e) {
						logger.error("Could not read package " + dir.getAbsolutePath(), e);
					}
				}
			} else {
				logger.warn("Multiple package entries for id=" + id);
			}
		}
	}

	public void refresh() {
		refresh(platformDir);
		refresh(usrDir);
	}
	
	private void refresh(File dir) {
		for(File f : dir.listFiles(f -> f.getName().endsWith(ZIP_SUFFIX))) {
			try {
				String filename = f.getName();
				String id = filename.substring(0, filename.length()-ZIP_SUFFIX.length());
				String md5new = GrowableBuffer.copyFrom(md5digest.digest(FileUtil.readFully(f).getBytes())).toHex();
				File md5file = new File(dir, id+MD5_SUFFIX);
				boolean update = false;
				if(!md5file.exists()) {
					logger.info("Adding new package '" + id + "'");
					update = true;
				} else if(!md5new.equals(FileUtil.readFully(md5file))) {
					logger.info("Updating package '" + id + "'");
					update = true;
				}
				if(update) {
					// FIXME unzip(md5file, new File(usrDir, id));
					FileUtil.writeFully(md5new, md5file);
				}
			} catch(IOException e) {
				logger.error("Could not process " + f.getAbsolutePath(), e);
			}
		}
	}

}
