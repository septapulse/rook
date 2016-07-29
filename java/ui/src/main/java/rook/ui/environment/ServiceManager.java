package rook.ui.environment;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rook.api.Service;
import rook.ui.websocket.message.PackageSupportInfo;
import rook.ui.websocket.message.ServiceSupportInfo;

/**
 * Uses reflection to find underlying {@link Service} implementations.
 * 
 * @author Eric Thill
 *
 */
public class ServiceManager {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final File[] servicesDirs;

	public ServiceManager(File[] servicesDirs) {
		this.servicesDirs = servicesDirs;
	}

	private void iterateJavaServices(String pkgFilter, Consumer<File> c) {
		for (File servicseDir : servicesDirs) {
			if (servicseDir.isDirectory()) {
				File javaDir = new File(servicseDir, "java");
				if (javaDir.isDirectory()) {
					// iterate through each java service package
					for (File serviceDir : javaDir.listFiles()) {
						if (serviceDir.isDirectory()) {
							// check filter
							if (pkgFilter == null || pkgFilter.equals(serviceDir.getName())) {
								// ensure there are libs to run
								File libDir = new File(serviceDir, "lib");
								if (libDir.isDirectory()) {
									c.accept(serviceDir);
								}
							}
						}
					}
				}
			}
		}
	}

	public List<String> getPackages() {
		final Set<String> result = new LinkedHashSet<>();
		iterateJavaServices(null, dir -> {
			// ensure there are libs to run
			File libDir = new File(dir, "lib");
			if (libDir.isDirectory()) {
				result.add(dir.getName());
			}
		});
		List<String> l = new ArrayList<>(result);
		l.sort((o1, o2) -> o1.compareTo(o2));
		return l;
	}

	public PackageSupportInfo getPackageInfo(String pkg) {
		PackageSupportInfo result = new PackageSupportInfo().setPkg(pkg).setServices(new ArrayList<>());
		iterateJavaServices(pkg, dir -> {
			File libDir = new File(dir, "lib");
			if (libDir.isDirectory()) {
				// lookup services in lib classpath
				URLClassLoader cl = null;
				try {
					// instantiate reflections
					cl = ServiceClasspathUtil.load(libDir);
					Configuration conf = new ConfigurationBuilder().addUrls(cl.getURLs()).addClassLoader(cl);
					Reflections reflect = new Reflections(conf);

					// lookup Service types
					Set<Class<? extends Service>> classes = reflect.getSubTypesOf(Service.class);
					for (Class<?> c : classes) {
						if (!Modifier.isAbstract(c.getModifiers()) && !Modifier.isInterface(c.getModifiers())) {
							String id = c.getName();
							String name = c.getSimpleName();
							result.getServices().add(new ServiceSupportInfo().setPkg(pkg).setId(id).setName(name));
						}
					}
					result.getServices().sort((s1, s2) -> s1.getName().compareTo(s2.getName()));
				} catch (Throwable t) {
					logger.error("Could not load services", t);
				} finally {
					if (cl != null) {
						try {
							cl.close();
						} catch (IOException e) {
							// we tried
						}
					}
				}
			}
		});
		return result;
	}

}
