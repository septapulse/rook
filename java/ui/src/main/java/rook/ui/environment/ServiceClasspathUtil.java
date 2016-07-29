package rook.ui.environment;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import rook.api.util.FileUtil;

/**
 * 
 * @author Eric Thill
 *
 */
class ServiceClasspathUtil {

	public static URLClassLoader load(File libDir) throws MalformedURLException {
		List<URL> urls = new ArrayList<>();
		for(File jar : libDir.listFiles(FileUtil.suffixFilter(".jar"))) {
			urls.add(jar.toURI().toURL());
		}
		return new URLClassLoader(urls.toArray(new URL[urls.size()]));
	}
}
