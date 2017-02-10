package run.rook.daemon.websocket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import run.rook.api.transport.GrowableBuffer;
import run.rook.api.util.FileUtil;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class ZipManager<T> {
	
	public static final String ROOK_CFG_FILENAME = "rook.cfg";
	
	private static final String ZIP_SUFFIX = ".zip";
	private static final String MD5_SUFFIX = ".md5";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Gson gson = new Gson();
	private final MessageDigest md5digest;
	private final Class<T> infoType;
	private final IdParser<T> idParser;
	private final IdSetter<T> idSetter;
	private final NameParser<T> nameParser;
	private final File[] readDirs;
	private final File managedDir;
	
	public ZipManager(Class<T> infoType, 
			IdParser<T> idParser, 
			IdSetter<T> idSetter, 
			NameParser<T> nameParser,
			File[] readDirs, 
			File managedDir) {
		this.infoType = infoType;
		this.idParser = idParser;
		this.idSetter = idSetter;
		this.nameParser = nameParser;
		try {
			md5digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new InstantiationError("Could not create MD5 digest");
		}
		this.readDirs = readDirs;
		this.managedDir = managedDir;
	}
	
	public void add(byte[] data) throws ZipManagerException {
		try {
			if(!managedDir.exists()) {
				managedDir.mkdirs();
			}
			String randomUUID = UUID.randomUUID().toString(); 
			File dest = new File(managedDir, randomUUID+ZIP_SUFFIX);
			FileOutputStream fos = new FileOutputStream(dest);
			fos.write(data);
			fos.close();
			refresh();
		} catch(IOException e) {
			throw new ZipManagerException("Could not add package", e);
		}
	}

	public boolean remove(String id) {
		File dir = new File(managedDir, id);
		File md5 = new File(managedDir, id+MD5_SUFFIX);
		File zip = new File(managedDir, id+ZIP_SUFFIX);
		if(FileUtil.delete(md5) | FileUtil.delete(dir) | FileUtil.delete(zip)) {
			refresh();
			return true;
		} else {
			return false;
		}
	}
	
	public T get(String id) {
		for(File dir : readDirs) {
			if(dir.isDirectory()) {
				T t = get(id, dir);
				if(t != null) {
					return t;
				}
			}
		}
		return null;
	}
	
	private T get(String id, File dir) {
		File pkgDir = new File(dir, id);
		if(pkgDir.isDirectory()) {
			File cfg = new File(pkgDir, ROOK_CFG_FILENAME);
			if(cfg.exists()) {
				try {
					T info = gson.fromJson(FileUtil.readFully(cfg), infoType);
					// override ID with actual directory ID
					idSetter.setId(info, id);
					return info;
				} catch(IOException e) {
					logger.warn("Could not read package " + dir.getAbsolutePath(), e);
				}
			}
		}
		return null;
	}

	public Collection<T> all() {
		List<T> packages = new ArrayList<>();
		for(File dir : readDirs) {
			if(dir.isDirectory()) {
				packages.addAll(all(dir));
			}
		}
		return packages;
	}
	
	private List<T> all(File dir) {
		List<T> packages = new ArrayList<>();
		for(File f : dir.listFiles(t -> t.isDirectory())) {
			String id = f.getName();
			File cfg = new File(f, ROOK_CFG_FILENAME);
			if(cfg.exists()) {
				try {
					T info = gson.fromJson(FileUtil.readFully(cfg), infoType);
					// override ID with actual directory ID
					idSetter.setId(info, id);
					packages.add(info);
				} catch(IOException e) {
					logger.error("Could not read package " + dir.getAbsolutePath(), e);
				}
			}
		}
		packages.sort(this::compareName);
		return packages;
	}
	
	private int compareName(T p1, T p2) {
		return nameParser.getName(p1).toLowerCase().compareTo(nameParser.getName(p2).toLowerCase());
	}

	public void refresh() {
		for(File dir : readDirs) {
			if(dir.isDirectory()) {
				refresh(dir);
			}
		}
	}
	
	private void refresh(File dir) {
		for(File f : dir.listFiles(f -> f.getName().endsWith(ZIP_SUFFIX))) {
			try {
				String filename = f.getName();
				String md5new = GrowableBuffer.copyFrom(md5digest.digest(FileUtil.readFully(f).getBytes())).toHex();
				File unzippedDir = new File(filename.substring(0, filename.length()-ZIP_SUFFIX.length()));
				File infoFile = new File(unzippedDir, ROOK_CFG_FILENAME);
				if(!infoFile.exists()) {
					// no matching directory of zip name: new package to process
					logger.info("Found new zip '" + filename + "'");
					// unzip package
					FileUtil.delete(unzippedDir);
					unzip(f, unzippedDir);
					if(!infoFile.exists()) {
						logger.error(f.getAbsolutePath() + " does not contain " + ROOK_CFG_FILENAME);
						break;
					}
					// read package info to get proper ID
					T info = gson.fromJson(FileUtil.readFully(infoFile), infoType);
					String id = idParser.getId(info);
					// check if a directory with this id already exists (use case: new version uploaded)
					File destDir = new File(dir, id);
					if(destDir.exists()) {
						logger.info("Updating package '" + id + "'");
						FileUtil.delete(destDir);
					} else {
						logger.info("Adding new package '" + id + "'");
					}
					// rename zip and directory to match id
					f.renameTo(new File(dir, id+ZIP_SUFFIX));
					unzippedDir.renameTo(destDir);
					// write md5 file
					File md5file = new File(dir, id+MD5_SUFFIX);
					FileUtil.writeFully(md5new, md5file);
				} else {
					// matching directory of zip name exists: existing package
					String id = unzippedDir.getName();
					File md5file = new File(dir, id+MD5_SUFFIX);
					if(!md5file.exists() || !md5new.equals(FileUtil.readFully(md5file))) {
						logger.info("Updating package '" + id + "'");
						// delete existing
						FileUtil.delete(unzippedDir);
						// unzip new
						unzip(f, unzippedDir);
						// write updated md5 file
						FileUtil.writeFully(md5new, md5file);
					}
				}
			} catch(Throwable t) {
				logger.error("Could not process " + f.getAbsolutePath(), t);
			}
		}
	}
	
	private void unzip(File source, File destination) throws IOException {
		try {
			ZipFile zipFile = new ZipFile(source);
	        zipFile.extractAll(destination.getAbsolutePath());
		} catch(ZipException e) {
			throw new IOException("Could not unzip " + source.getAbsolutePath(), e);
		}
	}
	

	public interface IdParser<T> {
		String getId(T o);
	}
	
	public interface IdSetter<T> {
		void setId(T o, String id);
	}
	
	public interface NameParser<T> {
		String getName(T o);
	}
}
