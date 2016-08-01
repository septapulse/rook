package rook.ui.environment;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rook.api.config.Configurable;
import rook.api.reflect.Instantiate;
import rook.api.util.FileUtil;
import rook.ui.websocket.message.ConfigInfo;
import rook.ui.websocket.message.TemplateField;
import rook.ui.websocket.message.TemplateObject;

/**
 * Get/Create/Manage configurations from the filesystem
 * 
 * @author Eric Thill
 *
 */
public class ConfigManager {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final File[] configDirs;
	private final File[] servicesDirs;

	public ConfigManager(File[] configDirs, File[] servicesDirs) {
		this.configDirs = configDirs;
		this.servicesDirs = servicesDirs;
	}

	public List<ConfigInfo> getServiceConfigs(String pkg, String sid) {
		List<ConfigInfo> results = new ArrayList<>();
		for (File configDir : configDirs) {
			if (configDir.isDirectory()) {
				File dir = new File(configDir, pkg + "/" + sid);
				if (dir.isDirectory()) {
					for (File cfgDir : dir.listFiles()) {
						File cfgFile = new File(cfgDir, "cfg");
						File idFile = new File(cfgDir, "id");
						if (cfgDir.isDirectory() && cfgFile.isFile() && idFile.isFile()) {
							results.add(new ConfigInfo().setPkg(pkg).setSid(sid).setConfigName(cfgDir.getName()));
						}
					}
				}
			}
		}
		results.sort((o1, o2) -> o1.getConfigName().compareTo(o2.getConfigName()));
		return results;
	}
	
	public TemplateObject getConfigTemplate(String pkg, String sid, String[] subPath) {
		try {
			TemplateObject t = new TemplateObject();
			for (File servicesDir : servicesDirs) {
				if (servicesDir.isDirectory()) {
					File javaDir = new File(servicesDir, "java");
					if (javaDir.isDirectory()) {
						File serviceDir = new File(javaDir, pkg);
						if (serviceDir.isDirectory()) {
							File libDir = new File(serviceDir, "lib");
							if (libDir.isDirectory()) {
								URLClassLoader cl = createClassLoader(libDir);
								try {
									Class<?> clazz = cl.loadClass(sid);
									Constructor<?> c = Instantiate.getConfigurableConstructor(clazz);
									if(c.getParameterTypes().length == 0) {
										return new TemplateObject();
									} else {
										return getConfigTemplate(c.getParameterTypes()[0], subPath);
									}
								} finally {
									try {
										cl.close();
									} catch (IOException e) {
										// nothing else we can do
									}
								}
							}
						}
					}
				}
			}
			return t;
		} catch(Throwable t) {
			logger.info("Could not retrieve config template. pkg=" + pkg + " sid=" + sid, t);
			return null;
		}
	}

	private TemplateObject getConfigTemplate(Class<?> type, String[] subPath) throws NoSuchFieldException, SecurityException {
		if(subPath != null) {
			for(String p : subPath) {
				Field f = type.getDeclaredField(p);
				if(f.getType() == List.class) {
					type = (Class<?>)((ParameterizedType)f.getGenericType()).getActualTypeArguments()[0];
				} else if(f.getType().isArray()) { 
					type = f.getType().getComponentType();
				} else {
					type = f.getType();
				}
			}
		}
		TemplateObject template = new TemplateObject();
		for(Field f : type.getDeclaredFields()) {
			if(!Modifier.isTransient(f.getModifiers())) {
				Class<?> t = f.getType();
				Configurable config = f.getAnnotation(Configurable.class);
				TemplateField tf = template.addField().setName(f.getName());
				tf.setComment(config == null ? "" : config.comment());
				if(t == long.class || t == Long.class
						|| t == int.class || t == Integer.class
						|| t == short.class || t == Short.class
						|| t == byte.class || t == Byte.class
						|| t == double.class || t == Double.class
						|| t == float.class || t == Float.class
						|| t == String.class) {
					if(config != null) {
						tf.setDefaultValue(config.defaultValue());
						if(config.min().length() > 0)
							tf.setMin(config.min());
						if(config.max().length() > 0)
							tf.setMax(config.max());
						if(config.increment().length() > 0)
							tf.setIncrement(config.increment());
					} else {
						tf.setDefaultValue("");
					}
				} else if(t.isArray() || t == List.class) {
					tf.setArray(true);
				} else {
					tf.setObject(true);
				}
			}
		}
		return template;
	}

	private URLClassLoader createClassLoader(File libDir) throws MalformedURLException {
		List<URL> urls = new ArrayList<>();
		for(File jar : libDir.listFiles(FileUtil.suffixFilter(".jar"))) {
			urls.add(jar.toURI().toURL());
		}
		return new URLClassLoader(urls.toArray(new URL[urls.size()]));
	}
	
	// public String getConfigTemplate(String pkg, String sid) throws Exception
	// {
	// URLClassLoader cl = null;
	// try {
	// Class<?> c = cl.loadClass(sid);
	// Constructor<?> constructor = Instantiate.getConfigurableConstructor(c);
	// Class<?> configType = constructor.getParameterTypes()[0];
	// StringBuilder json = new StringBuilder();
	// appendToServiceConfigTemplate(json, configType);
	// return json.toString();
	// } finally {
	// cl.close();
	// }
	// }
	//
	// private void appendToServiceConfigTemplate(StringBuilder json, Class<?>
	// type) {
	// json.append("{ ");
	// for(Field field : type.getDeclaredFields()) {
	// if(!Modifier.isTransient(field.getModifiers())) {
	// appendToServiceConfigTemplate(json, field);
	// }
	// }
	// json.setLength(json.length()-2);
	// json.append(" }");
	// }
	//
	// private void appendToServiceConfigTemplate(StringBuilder json, Field
	// field) {
	// json.append('"').append(field.getName()).append("\": { ");
	// if(CharSequence.class.isAssignableFrom(field.getType())) {
	// json.append("\"type\": \"String\", ");
	// Configurable c = field.getAnnotation(Configurable.class);
	// if(c != null) {
	// if(c.comment() != null && c.comment().length() > 0) {
	// json.append("\"comment\": \"").append(normalize(c.comment())).append("\",
	// ");
	// }
	// }
	// } else if(Boolean.class == field.getType() || boolean.class ==
	// field.getType()) {
	// json.append("\"type\": \"Boolean\", ");
	// Configurable c = field.getAnnotation(Configurable.class);
	// if(c != null) {
	// if(c.comment() != null && c.comment().length() > 0) {
	// json.append("\"comment\": \"").append(normalize(c.comment())).append("\",
	// ");
	// }
	// }
	// } else if(Number.class.isAssignableFrom(field.getType())) {
	// json.append("\"type\": \"Float\", ");
	// Configurable c = field.getAnnotation(Configurable.class);
	// ConfigurableInteger ci = field.getAnnotation(ConfigurableInteger.class);
	// ConfigurableFloat cf = field.getAnnotation(ConfigurableFloat.class);
	// if(ci != null) {
	// if(ci.comment() != null && ci.comment().length() > 0) {
	// json.append("\"comment\":
	// \"").append(normalize(ci.comment())).append("\", ");
	// }
	// if(ci.min() != ci.max()) {
	// json.append("\"min\": ").append(ci.min()).append(", ");
	// json.append("\"max\": ").append(ci.max()).append(", ");
	// }
	// if(ci.increment() != 0) {
	// json.append("\"increment\": ").append(ci.increment()).append(", ");
	// }
	// } else if(cf != null) {
	// if(cf.comment() != null && cf.comment().length() > 0) {
	// json.append("\"comment\":
	// \"").append(normalize(cf.comment())).append("\", ");
	// }
	// if(cf.min() != cf.max()) {
	// json.append("\"min\": ").append(cf.min()).append(", ");
	// json.append("\"max\": ").append(cf.max()).append(", ");
	// }
	// if(cf.increment() != 0) {
	// json.append("\"increment\": ").append(cf.increment()).append(", ");
	// }
	// } else if(c != null) {
	// if(c.comment() != null && c.comment().length() > 0) {
	// json.append("\"comment\": \"").append(normalize(c.comment())).append("\",
	// ");
	// }
	// }
	// } else if(Collection.class.isAssignableFrom(field.getType())) {
	// json.append("\"type\": \"List\", ");
	// Configurable cl = field.getAnnotation(Configurable.class);
	// if(cl != null && cl.comment() != null && cl.comment().length() > 0) {
	// json.append("\"comment\":
	// \"").append(normalize(cl.comment())).append("\", ");
	// }
	// json.append("\"subtype\": ");
	// appendToServiceConfigTemplate(json, (Class<?>)field.getGenericType());
	// } else if(field.isEnumConstant()) {
	// Object[] values = field.getType().getEnumConstants();
	// if(values != null && values.length > 0) {
	// json.append("\"values\": {");
	// for(Object e : values) {
	// json.append("\"").append(e.toString()).append("\", ");
	// }
	// json.append("}, ");
	// }
	// } else {
	// json.append("\"type\": \"Object\", ");
	// Configurable c = field.getAnnotation(Configurable.class);
	// if(c != null && c.comment() != null && c.comment().length() > 0) {
	// json.append("\"comment\": \"").append(normalize(c.comment())).append("\",
	// ");
	// }
	// json.append("\"fields\": ");
	// appendToServiceConfigTemplate(json, field.getType());
	// json.append(", ");
	// }
	// json.setLength(json.length()-2);
	// json.append(" }, ");
	// }
	//
	// private String normalize(String comment) {
	// StringBuilder sb = new StringBuilder();
	// for(int i = 0; i < comment.length(); i++) {
	// char c = comment.charAt(i);
	// comment.replace('"', '\'');
	// sb.append(c);
	// }
	// return sb.toString();
	// }
}
