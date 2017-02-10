package run.rook.test.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import run.rook.api.config.Configurable;

public class ServiceConfigGenerator {

	public static void main(String... args) throws Exception {
		Class<?> serviceType = Class.forName(args[0]);
		System.out.println(generate(serviceType, 1));
	}
	
	public static String generate(Class<?> serviceType, int tab) {
		Constructor<?> constructor = findConfigurableConstructor(serviceType);
		Class<?> configType = constructor.getParameterTypes()[0];
		StringBuilder sb = new StringBuilder();
		appendTab(sb, tab);
		sb.append("\"command\": \"java -cp lib/* io.septapulse.api.ServiceLauncher -st " + serviceType.getName() + " -sc ${config} -tt run.rook.core.transport.websocket.WebsocketTransport -tc {}\",\n");
		appendTab(sb, tab);
		sb.append("\"arguments\": {\n");
		sb.append(generate("config", configType, null, tab+1));
		sb.append("\n");
		appendTab(sb, tab);
		sb.append("}");
		return sb.toString();
	}
	
 	private static String generate(String name, Class<?> type, Field f, int tab) {
 		Configurable configurable = f == null ? null : f.getAnnotation(Configurable.class);
 		String defaultValue = configurable == null ? null : configurable.defaultValue();
		if(List.class.isAssignableFrom(type)) {
			return generateList(f, tab);
		} else if(isNumber(type)) {
			return generateNumber(name, defaultValue, false, tab);
		} else if(type.equals(String.class) || type.isEnum()) {
			// TODO don't treat enum as string
			return generateString(name, defaultValue, false,tab);
		} else if(type.equals(Boolean.class) || type.equals(boolean.class)) {
			return generateBoolean(name, defaultValue, false, tab);
		} else {
			return generateObject(name, type, false, tab);
		}
	}

	private static String generateList(Field field, int tab) {
		ParameterizedType pType = (ParameterizedType) field.getGenericType();
        Class<?> type = (Class<?>) pType.getActualTypeArguments()[0];
		if(isNumber(type)) {
			return generateNumber(field.getName(), null, true, tab);
		} else if(type.equals(String.class) || type.isEnum()) {
			// TODO don't treat enum as string
			return generateString(field.getName(), null, true, tab);
		} else if(type.equals(Boolean.class) || type.equals(boolean.class)) {
			return generateBoolean(field.getName(), null, true, tab);
		} else {
			return generateObject(field.getName(), type, true, tab);
		}
	}

	private static boolean isNumber(Class<?> type) {
		return byte.class.equals(type)
				|| short.class.equals(type)
				|| int.class.equals(type)
				|| long.class.equals(type)
				|| float.class.equals(type)
				|| double.class.equals(type)
				|| Byte.class.equals(type)
				|| Short.class.equals(type)
				|| Integer.class.equals(type)
				|| Long.class.equals(type)
				|| Float.class.equals(type)
				|| Double.class.equals(type);
	}
	
	private static String generateNumber(String name, String defaultValue, boolean list, int tab) {
		StringBuilder sb = new StringBuilder();
		appendTab(sb, tab);
		sb.append("\"" + name + "\": {\n");
		appendTab(sb, tab+1);
		sb.append("\"name\": \"" + name + "\",\n");
		appendTab(sb, tab+1);
		if(list) {
			sb.append("\"type\": \"NUMBER_LIST\"\n");
		} else {
			sb.append("\"type\": \"NUMBER\"\n");
		}
		if(defaultValue != null && defaultValue.length() > 0) {
			appendTab(sb, tab+1);
			sb.append("\"defaultValue\": \"" + defaultValue + "\"\n");
		}
		// FIXME min,max,increment
		appendTab(sb, tab);
		sb.append("}");
		return sb.toString();
	}
	
	private static String generateString(String name, String defaultValue, boolean list, int tab) {
		StringBuilder sb = new StringBuilder();
		appendTab(sb, tab);
		sb.append("\"" + name + "\": {\n");
		appendTab(sb, tab+1);
		sb.append("\"name\": \"" + name + "\",\n");
		appendTab(sb, tab+1);
		if(list) {
			sb.append("\"type\": \"STRING_LIST\"\n");
		} else {
			sb.append("\"type\": \"STRING\"\n");
		}
		if(defaultValue != null && defaultValue.length() > 0) {
			appendTab(sb, tab+1);
			sb.append("\"defaultValue\": \"" + defaultValue + "\"\n");
		}
		appendTab(sb, tab);
		sb.append("}");
		return sb.toString();
	}
	
	private static String generateBoolean(String name, String defaultValue, boolean list, int tab) {
		StringBuilder sb = new StringBuilder();
		appendTab(sb, tab);
		sb.append("\"" + name + "\": {\n");
		appendTab(sb, tab+1);
		sb.append("\"name\": \"" + name + "\",\n");
		appendTab(sb, tab+1);
		if(list) {
			sb.append("\"type\": \"BOOLEAN_LIST\"\n");
		} else {
			sb.append("\"type\": \"BOOLEAN\"\n");
		}
		if(defaultValue != null && defaultValue.length() > 0) {
			appendTab(sb, tab+1);
			sb.append("\"defaultValue\": \"" + defaultValue + "\"\n");
		}
		appendTab(sb, tab);
		sb.append("}");
		return sb.toString();
	}

	private static String generateObject(String name, Class<?> type, boolean list, int tab) {
		StringBuilder sb = new StringBuilder();
		appendTab(sb, tab);
		sb.append("\"" + name + "\": {\n");
		appendTab(sb, tab+1);
		sb.append("\"name\": \"" + name + "\",\n");
		appendTab(sb, tab+1);
		if(list) {
			sb.append("\"type\": \"OBJECT_LIST\"");
		} else {
			sb.append("\"type\": \"OBJECT\"");
		}
		if(type.getDeclaredFields().length > 0) {
			sb.append(",\n");
			appendTab(sb, tab+1);
			sb.append("\"children\": {\n");
			for(Field f : type.getDeclaredFields()) {
				if(!f.isSynthetic()) {
					sb.append(generate(f.getName(), f.getType(), f, tab+2));
					sb.append(",\n");
				}
			}
			sb.setLength(sb.length()-2); // remove last comma and newline
			sb.append("\n");
			appendTab(sb, tab+1);
			sb.append("}\n");
		} else {
			sb.append("\n");
		}
		appendTab(sb, tab);
		sb.append("}");
		return sb.toString();
	}

	private static void appendTab(StringBuilder sb, int tab) {
		for(int i = 0; i < tab; i++) {
			sb.append("  ");
		}
	}

	private static Constructor<?> findConfigurableConstructor(Class<?> type) {
		for(Constructor<?> c : type.getConstructors()) {
			if(c.isAnnotationPresent(Configurable.class)) {
				return c;
			}
		}
		throw new IllegalArgumentException(type.getName() + " does not have a @" + Configurable.class.getSimpleName() + " annotation");
	}
	
}
