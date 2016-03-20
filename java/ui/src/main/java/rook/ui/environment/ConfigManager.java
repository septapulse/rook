package rook.ui.environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;

import rook.api.RookRunner;
import rook.api.Service;
import rook.api.config.Configurable;
import rook.api.config.ConfigurableFloat;
import rook.api.config.ConfigurableInteger;

/**
 * Utility to manage configurations in a given directory
 * 
 * @author Eric Thill
 *
 */
public class ConfigManager {

	private final File configDirectory;
	private final File serviceDirectory;
	
	public ConfigManager(File configDirectory, File serviceDirectory) {
		this.configDirectory = configDirectory;
		this.serviceDirectory = serviceDirectory;
	}
	
	public Set<String> getConfigNames() {
		Set<String> result = new LinkedHashSet<>();
		for(File f : configDirectory.listFiles()) {
			if(f.isFile()) {
				result.add(f.getName());
			}
		}
		return result;
	}
	
	public String getConfig(String name) throws IOException {
		File f = new File(configDirectory, name);
		if(!f.exists()) {
			return "{ }";
		}
		StringBuilder json = new StringBuilder();
		BufferedReader r = new BufferedReader(new FileReader(f));
		String line;
		while((line = r.readLine()) != null) {
			json.append(line).append(" ");
		}
		r.close();
		return json.toString();
	}
	
	public void setConfig(String name, String cfg) throws IOException {
		FileWriter w = new FileWriter(new File(configDirectory, name));
		w.write(cfg);
		w.close();
	}
	
	public void deleteConfig(String name) throws IOException {
		new File(configDirectory, name).delete();
	}
	
	public Set<ServiceInfo> getServices() throws IOException {
		Set<ServiceInfo> results = new LinkedHashSet<>();
		for(File f : serviceDirectory.listFiles()) {
			if(f.isDirectory()) {
				String serviceLibrary = f.getName();
				URLClassLoader cl = new URLClassLoader(getServiceJars(serviceLibrary));
				Configuration config = new ConfigurationBuilder()
				         .setScanners(new SubTypesScanner(false))
				         .setUrls(getServiceJars(serviceLibrary))
				         .addClassLoader(cl);
				Reflections reflections = new Reflections(config);
				Set<Class<? extends Service>> serviceClasses = reflections.getSubTypesOf(Service.class);
				for(Class<? extends Service> c : serviceClasses) {
					if(!Modifier.isAbstract(c.getModifiers())) {
						String hierarchy = "/";
						Class<?> sup = c.getSuperclass();
						while(sup != null && Service.class.isAssignableFrom(sup)) {
							hierarchy = "/" + sup.getSimpleName() + hierarchy; 
							sup = sup.getSuperclass();
						}
						results.add(new ServiceInfo(serviceLibrary, c.getName(), hierarchy));
					}
				}
				cl.close();
			}
		}
		return results;
	}
	
	public String getConfigTemplate(String serviceLibrary, String serviceClass) throws Exception {
		URLClassLoader cl = new URLClassLoader(getServiceJars(serviceLibrary));
		try {
			Class<?> c = cl.loadClass(serviceClass);
			Constructor<?> constructor = RookRunner.getConfigurableConstructor(c);
			Class<?> configType = constructor.getParameterTypes()[0];
			StringBuilder json = new StringBuilder();
			appendToServiceConfigTemplate(json, configType);
			return json.toString();
		} finally {
			cl.close();
		}
	}
	
	private URL[] getServiceJars(String serviceLibrary) throws MalformedURLException {
		List<URL> urls = new ArrayList<>();
		for(File f : new File(serviceDirectory, serviceLibrary).listFiles()) {
			if(f.getName().endsWith(".jar")) {
				urls.add(f.toURI().toURL());
			}
		}
		return urls.toArray(new URL[urls.size()]);
	}

	private void appendToServiceConfigTemplate(StringBuilder json, Class<?> type) {
		json.append("{  ");
		for(Field field : type.getDeclaredFields()) {
			if(!Modifier.isTransient(field.getModifiers())) {
				appendToServiceConfigTemplate(json, field);
			}
		}
		json.setLength(json.length()-2);
		json.append(" }");
	}
	
	private void appendToServiceConfigTemplate(StringBuilder json, Field field) {
		json.append('"').append(field.getName()).append("\": {  ");
		if(CharSequence.class.isAssignableFrom(field.getType())) {
			json.append("\"type\": \"String\", ");
			Configurable c = field.getAnnotation(Configurable.class);
			if(c != null) {
				if(c.comment() != null && c.comment().length() > 0) {
					json.append("\"comment\": \"").append(normalize(c.comment())).append("\", ");
				}	
			}
		} else if(Boolean.class == field.getType() || boolean.class == field.getType()) { 
			json.append("\"type\": \"Boolean\", ");
			Configurable c = field.getAnnotation(Configurable.class);
			if(c != null) {
				if(c.comment() != null && c.comment().length() > 0) {
					json.append("\"comment\": \"").append(normalize(c.comment())).append("\", ");
				}	
			}
		} else if(Number.class.isAssignableFrom(field.getType())) {
			json.append("\"type\": \"Float\", ");
			Configurable c = field.getAnnotation(Configurable.class);
			ConfigurableInteger ci = field.getAnnotation(ConfigurableInteger.class);
			ConfigurableFloat cf = field.getAnnotation(ConfigurableFloat.class);
			if(ci != null) {
				if(ci.comment() != null && ci.comment().length() > 0) {
					json.append("\"comment\": \"").append(normalize(ci.comment())).append("\", ");
				}	
				if(ci.min() != ci.max()) {
					json.append("\"min\": ").append(ci.min()).append(", ");
					json.append("\"max\": ").append(ci.max()).append(", ");
				}
				if(ci.increment() != 0) {
					json.append("\"increment\": ").append(ci.increment()).append(", ");
				}
			} else if(cf != null) {
				if(cf.comment() != null && cf.comment().length() > 0) {
					json.append("\"comment\": \"").append(normalize(cf.comment())).append("\", ");
				}	
				if(cf.min() != cf.max()) {
					json.append("\"min\": ").append(cf.min()).append(", ");
					json.append("\"max\": ").append(cf.max()).append(", ");
				}
				if(cf.increment() != 0) {
					json.append("\"increment\": ").append(cf.increment()).append(", ");
				}
			} else if(c != null) {
				if(c.comment() != null && c.comment().length() > 0) {
					json.append("\"comment\": \"").append(normalize(c.comment())).append("\", ");
				}
			}
		} else if(Collection.class.isAssignableFrom(field.getType())) {
			json.append("\"type\": \"List\", ");
			Configurable cl = field.getAnnotation(Configurable.class);
			if(cl != null && cl.comment() != null && cl.comment().length() > 0) {
				json.append("\"comment\": \"").append(normalize(cl.comment())).append("\", ");
			}
			json.append("\"subtype\": ");
			appendToServiceConfigTemplate(json, (Class<?>)field.getGenericType());
		} else if(field.isEnumConstant()) {
			Object[] values = field.getType().getEnumConstants();
			if(values != null && values.length > 0) {
				json.append("\"values\": {");
				for(Object e : values) {
					json.append("\"").append(e.toString()).append("\", ");
				}
				json.append("}, ");
			}
		} else {
			json.append("\"type\": \"Object\", ");
			Configurable c = field.getAnnotation(Configurable.class);
			if(c != null && c.comment() != null && c.comment().length() > 0) {
				json.append("\"comment\": \"").append(normalize(c.comment())).append("\", ");
			}
			json.append("\"fields\": ");
			appendToServiceConfigTemplate(json, field.getType());
			json.append(", ");
		}
		json.setLength(json.length()-2);
		json.append(" }, ");
	}

	private String normalize(String comment) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < comment.length(); i++) {
			char c = comment.charAt(i);
			comment.replace('"', '\'');
			sb.append(c);
		}
		return sb.toString();
	}
}
