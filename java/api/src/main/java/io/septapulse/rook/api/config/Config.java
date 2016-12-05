package io.septapulse.rook.api.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import io.septapulse.rook.api.util.FileUtil;

/**
 * Utility methods to parse/read configurations
 * 
 * @author Eric Thill
 *
 */
public class Config {

	private static final String FILE_PREFIX = "file://";
	private static final String RESOURCE_PREFIX = "resource://";

	/**
	 * Resolves the give configuration string. <br>
	 * If the string starts with "file://", the associated file on the
	 * filesystem will be read fully and its contents returned.<br>
	 * If the string starts with "resource://", the associated JVM resource will
	 * be read using getClass().getClassLoader().getResourceAsStream(), and if
	 * that fails, using
	 * {@link ClassLoader#getSystemResourceAsStream(String)}<br>
	 * If the string doesn't start with "file://" or "resource://" the string
	 * will be returned as-is.
	 * 
	 * @param s
	 *            The configuration string to read/parse
	 * @return The read/parsed configuration as a string
	 * @throws IOException
	 */
	public static String parse(String s) throws IOException {
		if (s == null) {
			return null;
		} else if (s.startsWith(FILE_PREFIX)) {
			String file = s.substring(FILE_PREFIX.length());
			return Config.readFile(file);
		} else if (s.startsWith(RESOURCE_PREFIX)) {
			String resource = s.substring(RESOURCE_PREFIX.length());
			return Config.readResource(resource);
		} else {
			return s;
		}
	}

	/**
	 * Fully read a file
	 * 
	 * @param file
	 *            The path of the file to read
	 * @return The file content
	 * @throws IOException
	 */
	public static String readFile(String file) throws IOException {
		return FileUtil.readFully(new File(file));
	}

	/**
	 * Fully read a resouce
	 * 
	 * @param resource
	 *            The resource to read
	 * @return The resource content
	 * @throws IOException
	 */
	public static String readResource(String resource) throws IOException {
		InputStream in = Config.class.getClassLoader().getResourceAsStream(resource);
		if (in == null)
			in = ClassLoader.getSystemResourceAsStream(resource);
		if (in == null)
			throw new IOException("Resource not found: " + resource);
		StringBuilder sb = new StringBuilder();
		int v;
		while ((v = in.read()) != -1) {
			sb.append((char) v);
		}
		return sb.toString();
	}

}
