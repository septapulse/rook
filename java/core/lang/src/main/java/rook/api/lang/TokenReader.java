package rook.api.lang;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
class TokenReader {

	private final List<String> tokens;
	private int off = 0;
	
	public TokenReader(List<String> tokens) {
		this.tokens = tokens;
	}
	
	public TokenReader copy() {
		TokenReader copy = new TokenReader(tokens);
		copy.off = off;
		return copy;
	}
	
	public void reset() {
		off = 0;
	}
	
	public boolean hasNext() {
		return off < tokens.size();
	}
	
	public String next() {
		if(off < tokens.size()) {
			return tokens.get(off++);
		} else {
			return null;
		}
	}
	
	public String peek() {
		return peek(0);
	}
	
	public String peek(int i) {
		if(off+i < tokens.size()) {
			return tokens.get(off+i);
		} else {
			return null;
		}
	}
	
	public TokenReader skip() {
		return skip(1);
	}
			
	public TokenReader skip(int num) {
		for(int i = 0; i < num; i++) {
			next();
		}
		return this;
	}
	
	public String mergeWithNext() {
		tokens.set(off, tokens.get(off)+tokens.get(off+1));
		tokens.remove(off+1);
		return tokens.get(off);
	}
	
	public TokenReader nextAsReader() {
		return nextAsReader(1);
	}
	
	public TokenReader nextAsReader(int numTokens) {
		List<String> t = new ArrayList<>();
		for(int i = 0; i < numTokens; i++) {
			t.add(next());
		}
		return new TokenReader(t);
	}
	
	public static TokenReader tokenize(String code) {
		List<String> tokens = new ArrayList<String>();
		AtomicInteger off = new AtomicInteger();
		while(off.get() < code.length()) {
			String token = nextToken(code, off);
			if(token != null) {
				tokens.add(token);
			}
		}
		return new TokenReader(tokens);
	}
	
	private static String nextToken(String code, AtomicInteger off) {
		while(off.get() < code.length() && Character.isWhitespace(code.charAt(off.get()))) {
			off.incrementAndGet();
		}
		if(off.get() == code.length()) {
			return null;
		}
		
		int start = off.get();
		
		// will be at least 1 character
		char firstChar = code.charAt(off.get());
		off.incrementAndGet();
		
		if('"' == firstChar) {
			while(code.charAt(off.get()) != '"') {
				off.incrementAndGet();
			}
			off.incrementAndGet();
		} else if(isSymbol(firstChar)) {
			// read a potential 2-letter symbol
			if((firstChar == '<' || firstChar == '>' || firstChar == '!' || firstChar == '=' || firstChar == '+' || firstChar == '-')
					&& off.get() < code.length() 
					&& !Character.isWhitespace(code.charAt(off.get()))
					&& isSymbol(code.charAt(off.get()))
			) {
				off.incrementAndGet();
			}
		} else {
			// read a word
			while(off.get() < code.length() 
					&& !Character.isWhitespace(code.charAt(off.get()))
					&& !isSymbol(code.charAt(off.get()))
			) {
				off.incrementAndGet();
			}
		}
		
		return code.substring(start, off.get());
	}

	private static boolean isSymbol(char firstChar) {
		switch(firstChar) {
		case '!':
		case '=':
		case '<':
		case '>':
		case '&':
		case '|':
		case '(':
		case ')':
		case '{':
		case '}':
		case '+':
		case '-':
		case '*':
		case '/':
		case '%':
		case ',':
			return true;
		default:
			return false;
		}
	}

	@Override
	public String toString() {
		return "TokenReader [tokens=" + tokens + ", off=" + off + "]";
	}
	
}
