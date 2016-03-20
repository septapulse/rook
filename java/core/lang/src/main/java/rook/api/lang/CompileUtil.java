package rook.api.lang;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
class CompileUtil {

	private CompileUtil() {
		
	}
	
	public static boolean isKeyword(String s) {
		if(s == null) {
			return false;
		}
		switch(s) {
		case "if":
		case "then":
		case "else":
		case "while":
		case "true":
		case "false":
		case "function":
		case ",":
			return true;
		default:
			return false;
		}
	}

	public static TokenReader readGroup(TokenReader tokenReader, String open, String close) throws CompilationException {
		int stack = 1;
		int groupNumTokens = 0;
		while(stack > 0) {
			String t = tokenReader.peek(groupNumTokens++);
			if(open.equals(t)) {
				stack++;
			} else if(close.equals(t)) {
				stack--;
			} else if(null == t) {
				throw new CompilationException("Closing '" + close + "' not found");
			}
		}
		TokenReader block = tokenReader.nextAsReader(groupNumTokens-1);
		tokenReader.next(); // skip final ")"
		return block;
	}
	
	public static boolean isValue(String s) {
		return isBoolean(s) || isNumber(s) || s.startsWith("\"");
	}
	
	public static boolean isBoolean(String s) {
		return s.equals("true") || s.equals("false");
	}

	public static boolean isNumber(String s) {
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(c == '.') {
				continue;
			} else if(c >= '0' && c <= '9') {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}
	
	public static  boolean isInteger(String s) {
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}
	
	public static  boolean isAlphanumeric(String s) {
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(!Character.isAlphabetic(c) && (c < '0' || c > '9')) {
				return false;
			}
		}
		return true;
	}
	
	public static int getPrecedence(Object lookAhead) {
		if(lookAhead == null || lookAhead.getClass() != String.class) {
			return 1;
		}
		switch(lookAhead.toString()) {
		case "++":
		case "--":
			return 10;
		case "!":
			return 9;
		case "*":
		case "/":
		case "%":
			return 8;
		case "+":
		case "-":
			return 7;
		case "<":
		case "<=":
		case ">":
		case ">=":
			return 6;
		case "==":
		case "!=":
			return 5;
		case "&&":
			return 4;
		case "||":
			return 3;
		case "=":
			return 2;
		default:
			return 1;
		}
	}
	
	public static boolean isOperator(String s) {
		return isBinaryOperator(s) || isRightAssociativeOperator(s);
	}
	
	public static boolean isRightAssociativeOperator(Object s) {
		if(s == null || s.getClass() != String.class) {
			return false;
		}
		switch(s.toString()) {
		case "!":
		case "=":
			return true;
		default:
			return false;
		}
	}

	public static boolean isBinaryOperator(Object s) {
		if(s == null || s.getClass() != String.class) {
			return false;
		}
		switch(s.toString()) {
		case "*":
		case "/":
		case "%":
		case "+":
		case "-":
		case "<":
		case "<=":
		case ">":
		case ">=":
		case "==":
		case "!=":
		case "&&":
		case "||":
			return true;
		default:
			return false;
		}
	}
}
