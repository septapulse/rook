package run.rook.api.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Utility methods for file operations
 * 
 * @author Eric Thill
 *
 */
public class FileUtil {

	/**
	 * Create a {@link FileFilter} for the given file suffix
	 * 
	 * @param suffix
	 * @return
	 */
	public static FileFilter suffixFilter(String suffix) {
		return f -> f.getName().endsWith(suffix);
	}

	/**
	 * Fully read the given file as a String
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static String readFully(File file) throws IOException {
		StringBuilder sb = new StringBuilder();
		FileReader r = new FileReader(file);
		try {
			int v;
			while ((v = r.read()) != -1) {
				sb.append((char) v);
			}
			return sb.toString();
		} finally {
			r.close();
		}
	}
	
	/**
	 * Write a string to a file, overriding current contents
	 * 
	 * @param str
	 * @param dest
	 * @throws IOException
	 */
	public static void writeFully(String str, File dest) throws IOException {
		FileWriter w = new FileWriter(dest);
		w.write(str);
		w.close();
	}

	/**
	 * Recursively delete the given file/directory
	 * 
	 * @param file
	 */
	public static boolean delete(File file) {
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				delete(f);
			}
		}
		return file.delete();
	}

	/**
	 * Override the given file with the given file contents
	 * 
	 * @param file
	 * @param content
	 * @throws IOException
	 */
	public static void replaceFileContents(File file, String content) throws IOException {
		Writer w = null;
		try {
			w = new FileWriter(file);
			w.write(content);
		} finally {
			if (w != null)
				w.close();
		}
	}
}
