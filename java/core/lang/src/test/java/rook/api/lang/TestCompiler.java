package rook.api.lang;

public class TestCompiler {

public static void main(String[] args) throws CompilationException {
		String code = ""
//				+ "\n" + "sleep(2000)"
//				+ "\n" + "print(\"hello " + "world!\")"
//				+ "\n" + "print(test())"
//				+ "\n" + "function test() {"
//				+ "\n" + "  var1 = 1"
//				+ "\n" + "  var2 = 2"
//				+ "\n" + "  var3 = add(var1, increment(var2))"
//				+ "\n" + "  var3"
//				+ "\n" + "}"
//				+ "\n" + "function add(var1, var2) {"
//				+ "\n" + "  var1 + var2"
//				+ "\n" + "}"
//				+ "\n" + "function increment(var) {"
//				+ "\n" + "  var+1"
//				+ "\n" + "}"
				+ "\n" + "TEST = -1"
				+ "\n" + "while(true) {"
				+ "\n" + "  print(\"HELLO WORLD!\")"
				+ "\n" + "  print(TEST)"
				+ "\n" + "  sleep(1000)"
				+ "\n" + "}"
				;
		
		System.out.println(code);
		System.out.println("----------------------------------------");
		Application app = Compiler.compile(code);
		System.out.println("----------------------------------------");
		GlobalScope scope = new GlobalScope();
//		vars.put("var1", new Variable().set(12345));
		scope.setByValue("var2", new Variable().set(12345));
		System.out.println(app.toString(true));
		System.out.println("----------------------------------------");
		Variable returnValue = app.execute(scope);
		System.out.println("----------------------------------------");
		System.out.println(returnValue);
	}
}
