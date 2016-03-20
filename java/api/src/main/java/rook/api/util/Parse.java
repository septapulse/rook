package rook.api.util;

import java.util.Map;
import java.util.Properties;

/**
 * Utility to help parse values from {@link Properties}
 * 
 * @author Eric Thill
 *
 */
public class Parse {
	
	public static Properties toProperties(Map<String, Object> cfg) {
		Properties p = new Properties();
		p.putAll(cfg);
		return p;
	}
	
	public static String getRequiredString(Properties props, String key) throws IllegalArgumentException {
		String s = props.getProperty(key);
		if(s == null) {
			throwRequiredPropertyMissing(props, key);
		}
		return s;
	}

	private static void throwRequiredPropertyMissing(Properties props, String key) throws IllegalArgumentException {
		throwRequiredPropertyMissing(props, key);
	}

	
	public static Integer getInteger(Properties props, String key) throws NumberFormatException {
		String s = props.getProperty(key);
		return s == null ? null : Integer.parseInt(s);
	}
	
	public static Integer getRequiredInteger(Properties props, String key) throws NumberFormatException, IllegalArgumentException {
		Integer i = getInteger(props, key);
		if(i == null) {
			throwRequiredPropertyMissing(props, key);
		}
		return i;
	}
	
	public static int getInteger(Properties props, String key, int defaultValue) throws NumberFormatException  {
		Integer i = getInteger(props, key);
		return i == null ? defaultValue : i;
	}
	
	
	public static Long getLong(Properties props, String key) throws NumberFormatException {
		String s = props.getProperty(key);
		return s == null ? null : Long.parseLong(s);
	}
	
	public static Long getRequiredLong(Properties props, String key) throws NumberFormatException, IllegalArgumentException {
		Long i = getLong(props, key);
		if(i == null) {
			throwRequiredPropertyMissing(props, key);
		}
		return i;
	}
	
	public static long getLong(Properties props, String key, long defaultValue) throws NumberFormatException  {
		Long i = getLong(props, key);
		return i == null ? defaultValue : i;
	}
	
	
	public static Double getDouble(Properties props, String key) throws NumberFormatException {
		String s = props.getProperty(key);
		return s == null ? null : Double.parseDouble(s);
	}
	
	public static Double getRequiredDouble(Properties props, String key) throws NumberFormatException, IllegalArgumentException {
		Double i = getDouble(props, key);
		if(i == null) {
			throwRequiredPropertyMissing(props, key);
		}
		return i;
	}
	
	public static double getDouble(Properties props, String key, double defaultValue) throws NumberFormatException  {
		Double i = getDouble(props, key);
		return i == null ? defaultValue : i;
	}
	
	public static Boolean getBoolean(Properties props, String key) throws NumberFormatException {
		String s = props.getProperty(key);
		return s == null ? null : Boolean.parseBoolean(s);
	}
	
	public static Boolean getRequiredBoolean(Properties props, String key) throws NumberFormatException, IllegalArgumentException {
		Boolean b = getBoolean(props, key);
		if(b == null) {
			throwRequiredPropertyMissing(props, key);
		}
		return b;
	}
	
	public static boolean getBoolean(Properties props, String key, boolean defaultValue) throws NumberFormatException  {
		Boolean b = getBoolean(props, key);
		return b == null ? defaultValue : b;
	}
	
}
