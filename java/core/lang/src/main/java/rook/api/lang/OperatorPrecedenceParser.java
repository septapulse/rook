package rook.api.lang;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
class OperatorPrecedenceParser {

	// https://en.wikipedia.org/wiki/Operator-precedence_parser#Pseudo-code
	
	public String parse(TokenReader tr) throws CompilationException {
		return parse(nextToken(tr), 0, tr).toString();
	}
	
	public String parse(String lhs, TokenReader tr) throws CompilationException {
		return parse(lhs, 0, tr).toString();
	}
	
	private Object parse(Object lhs, int minPrecedence, TokenReader tr) throws CompilationException {
		Object lookAhead = peekToken(tr);
		while(CompileUtil.isBinaryOperator(lookAhead) && CompileUtil.getPrecedence(lookAhead) >= minPrecedence) {
			Object op = lookAhead;
			nextToken(tr);
			Object rhs = nextToken(tr);
			lookAhead = peekToken(tr);
			while((CompileUtil.isBinaryOperator(lookAhead) && CompileUtil.getPrecedence(lookAhead) > CompileUtil.getPrecedence(op))
					|| (CompileUtil.isRightAssociativeOperator(lookAhead) && CompileUtil.getPrecedence(lookAhead) == CompileUtil.getPrecedence(op))) {
				rhs = parse(rhs, CompileUtil.getPrecedence(lookAhead), tr);
				lookAhead = peekToken(tr);
			}
			lhs = new Node(lhs, op.toString(), rhs);
		}
		return lhs;
	}
	
	private Object nextToken(TokenReader tr) throws CompilationException {
		String t = tr.next();
		if(CompileUtil.isValue(t)) {
			return t;
		} else if(CompileUtil.isKeyword(t)) {
			throw new UnsupportedOperationException("Keyword when none was expected");
		} else if("(".equals(t)) {
			return parse(CompileUtil.readGroup(tr, "(", ")"));
		} else {
			return t;
		}
	}
	
	private Object peekToken(TokenReader tokenReader) throws CompilationException {
		String t = tokenReader.peek();
		if("(".equals(t)) {
			return parse(CompileUtil.readGroup(tokenReader.copy(), "(", ")"));
		} else {
			return t;
		}
	}
	
	private static class Node {
		private final Object lhs;
		private final String operator;
		private final Object rhs;
		
		public Node(Object lhs, String operator, Object rhs) {
			this.lhs = lhs;
			this.operator = operator;
			this.rhs = rhs;
		}
		@Override
		public String toString() {
			if(rhs == null) {
				return lhs.toString();
			} else if(lhs == null) {
				return rhs.toString();
			} else {
				return "(" + lhs + " " + operator + " " + rhs + ")";
			}
		}
	}

	
}
