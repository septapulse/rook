package run.rook.test.tools;

import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.Set;

import org.reflections.Reflections;

import run.rook.api.Service;

public class PackageConfigGenerator {
	
	public static void main(String... args) {
		String id = args[0];
		String name = args[1];
		String packagePrefix = args[2];
		generate(id, name, packagePrefix, System.out);
	}
	
	public static void generate(String id, String name, String packagePrefix, PrintStream p) {
		Reflections reflections = new Reflections("");    
		Set<Class<? extends Service>> classes = reflections.getSubTypesOf(Service.class);
		p.println("{");
		p.println("  \"id\": \"" + id + "\",");
		p.println("  \"name\": \"" + name + "\",");
		p.println("  \"services\": {");
		boolean first = true;
		for(Class<? extends Service> c : classes) {
			if(c.getName().startsWith(packagePrefix) && !c.isInterface() && !Modifier.isAbstract(c.getModifiers())) {
				if(first) {
					first = false;
				} else {
					p.println(",");
				}
				p.println("    \"" + c.getName() + "\": {");
				p.println("      \"id\": \"" + c.getName() + "\",");
				p.println("      \"name\": \"" + generateName(c.getSimpleName()) + "\",");
				p.println(ServiceConfigGenerator.generate(c, 3));
				p.print("    }");
			}
		}
		p.println();
		p.println("  }");
		p.println("}");
	}

	private static String generateName(String str) {
		StringBuilder sb = new StringBuilder();
		boolean lastCap = true;
		for(int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if(Character.isUpperCase(c) && !lastCap) {
				sb.append(" ");
			}
			sb.append(c);
			lastCap = Character.isUpperCase(c);
		}
		String s = sb.toString();
		if(s.endsWith("Service")) {
			s = s.substring(0, s.length()-"Service".length());
		}
		return s.trim();
	}
	
}
