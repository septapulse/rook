package rook.api.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rook.api.lang.operations.AddOperation;
import rook.api.lang.operations.AndOperation;
import rook.api.lang.operations.AssignOperation;
import rook.api.lang.operations.BangOperation;
import rook.api.lang.operations.ConstantOperation;
import rook.api.lang.operations.DivideOperation;
import rook.api.lang.operations.EqualsOperation;
import rook.api.lang.operations.FunctionCallOperation;
import rook.api.lang.operations.GreaterThanOperation;
import rook.api.lang.operations.GreaterThanOrEqualToOperation;
import rook.api.lang.operations.GroupOperation;
import rook.api.lang.operations.IfOperation;
import rook.api.lang.operations.LessThanOperation;
import rook.api.lang.operations.LessThanOrEqualToOperation;
import rook.api.lang.operations.LogOperation;
import rook.api.lang.operations.ModuloOperation;
import rook.api.lang.operations.MultiplyOperation;
import rook.api.lang.operations.OrOperation;
import rook.api.lang.operations.PostDecrementOperation;
import rook.api.lang.operations.PostIncrementOperation;
import rook.api.lang.operations.PrintOperation;
import rook.api.lang.operations.ReturnOperation;
import rook.api.lang.operations.ScopedOperation;
import rook.api.lang.operations.SleepOperation;
import rook.api.lang.operations.SubtractOperation;
import rook.api.lang.operations.WhileOperation;

/**
 * Compiles a rook script and returns an {@link Application} object
 * 
 * @author Eric Thill
 *
 */
public class Compiler {
	
	public static Application compile(String code) throws CompilationException {
		return compile(code, System.out::println);
	}
	
	public static Application compile(String code, Consumer<String> consoleLogger) throws CompilationException {
		return new Compiler(code, consoleLogger).compile();
	}
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final OperatorPrecedenceParser opPrecedenceParser = new OperatorPrecedenceParser();
	private final FunctionManager functionManager = new FunctionManager();
	private final String code;
	private final Consumer<String> consoleLogger;
	
	private Compiler(String code, Consumer<String> consoleLogger) {
		this.code = code;
		this.consoleLogger = consoleLogger;
	}
		
	private Application compile() throws CompilationException {
		try {
			TokenReader tr = TokenReader.tokenize(code);
			defineSystemFunctions();
			return compile(tr);
		} catch(CompilationException e) {
			throw e;
		} catch(Throwable t) {
			throw new CompilationException(t);
		}
	}
	
	private void defineSystemFunctions() throws CompilationException {
		functionManager.addFunction("sleep", 1, 
				new Function("sleep", Arrays.asList(new String[] { "millis" }), 
						new SleepOperation(), true));
		functionManager.addFunction("print", 1, 
				new Function("print", Arrays.asList(new String[] { "s" }), 
						new PrintOperation(consoleLogger), true));
		functionManager.addFunction("log", 1, 
				new Function("log", Arrays.asList(new String[] { "s" }), 
						new LogOperation(), true));
	}

	private Application compile(final TokenReader tr) throws CompilationException {
		if(logger.isDebugEnabled()) logger.debug("compile " + tr);

		// compile main scope
		List<Operation> ops = new ArrayList<>();
		while(tr.hasNext() && !"function".equals(tr.peek())) {
			Operation op = compileNext(tr);
			if(op != null) {
				ops.add(op);
			}
		}
		Operation main = new ScopedOperation(ops, false);
		
		while(tr.hasNext()) {
			if(!"function".equals(tr.next())) {
				throw new CompilationException("Illegal token between function definitions: " + tr.peek());
			}
			compileFunction(tr);
		}
		
		return new Application(functionManager, main);
	}

	private Operation compileNext(TokenReader tr) throws CompilationException {
		if(logger.isDebugEnabled()) logger.debug("compileNext " + tr);
		String t = tr.peek();
		if("{".equals(t)) {
			return compileScope(tr.skip());
		} else if(CompileUtil.isKeyword(t)) {
			return compileKeyword(tr.next(), tr);
		} else if("!".equals(t)) {
			return compileBang(tr.skip());
		} else if("=".equals(tr.peek(1))) {
			return compileAssignment(t, tr.skip(2));
		} else if("++".equals(tr.peek(1))) {
			tr.skip(2);
			return new PostIncrementOperation(t);
		} else if("--".equals(tr.peek(1))) {
			tr.skip(2);
			return new PostDecrementOperation(t);
		} else if(CompileUtil.isAlphanumeric(t) && "(".equals(tr.peek(1))) {
			return compileFunctionCall(t, tr.skip(2));
		} else {
			return compileUnprecedendedOperation(tr);
		}
	}
	
	private Operation compileScope(TokenReader tr) throws CompilationException {
		tr = CompileUtil.readGroup(tr, "{", "}");
		if(logger.isDebugEnabled()) logger.debug("compileScope " + tr);
		List<Operation> ops = new ArrayList<>();
		while (tr.hasNext()) {
			Operation op = compileNext(tr);
			if(op != null) {
				ops.add(op);
			}
		}
		return new ScopedOperation(ops, true);
	}

	private Operation compileFunctionCall(String functionName, TokenReader tr) throws CompilationException {
		if(logger.isDebugEnabled()) logger.debug("compileFunctionCall " + functionName + " " + tr);
		if(")".equals(tr.peek())) {
			tr.next();
			return new FunctionCallOperation(functionName, Collections.emptyList(), functionManager);
		}
		List<Operation> input = new ArrayList<>();
		input.add(compileNext(tr));

		while(true) {
			String t = tr.next();
			if(")".equals(t)) {
				return new FunctionCallOperation(functionName, input, functionManager);
			} else if(",".equals(t)) {
				input.add(compileNext(tr));
			} else {
				throw new CompilationException("Invalid token in function call: " + t);
			}
		}
	}

	private Operation compileBang(TokenReader tr) throws CompilationException {
		if(logger.isDebugEnabled()) logger.debug("compileBang " + tr);
		return new BangOperation(compileNext(tr));
	}

	private Operation compileKeyword(String keyword, TokenReader tr) throws CompilationException {
		if(logger.isDebugEnabled()) logger.debug("compileKeyword " + keyword + " " + tr);
		if ("function".equals(keyword)) {
			throw new CompilationException("Cannot define function inline");
		} else if ("if".equals(keyword)) {
			Operation condition = compileUnprecedendedOperation(tr);
			
			if("then".equals(tr.peek())) {
				tr.next();
			}
			
			Operation thenOp = compileNext(tr);
			if("else".equals(tr.peek())) {
				tr.next();
				Operation elseOp = compileNext(tr);
				return new IfOperation(condition, thenOp, elseOp);
			} else {
				return new IfOperation(condition, thenOp, null);
			}
		} else if ("while".equals(keyword)) {
			Operation condition = compileUnprecedendedOperation(tr);
			Operation op = compileNext(tr);
			return new WhileOperation(condition, op);
		} else if ("while".equals(keyword)) {
			throw new CompilationException("while is not implemented yet");
		} else if("true".equals(keyword) || "false".equals(keyword)) {
			return new ConstantOperation(new Variable().set(Boolean.parseBoolean(keyword)));
		}  else {
			throw new CompilationException("bad keyword: " + keyword);
		}
	}
	
	private Operation compileAssignment(String var, TokenReader tr) throws CompilationException {
		if(logger.isDebugEnabled()) logger.debug("compileAssignment " + var + "= " + tr);
		return new AssignOperation(var, compileNext(tr));
	}

	private Operation compileUnprecedendedOperation(TokenReader tr) throws CompilationException {
		boolean groupOperation = false;
		if("(".equals(tr.peek())) {
			tr.next();
			tr = CompileUtil.readGroup(tr, "(", ")");
			groupOperation = true;
		} else {
			if(logger.isDebugEnabled()) logger.debug("compileUnprecedendedOperation " + tr);
			int len = 0;
			while(true) {
				if(tr.peek(len) == null || CompileUtil.isKeyword(tr.peek(len))) {
					break;
				}
				if("-".equals(tr.peek(len)) && CompileUtil.isNumber(tr.peek(len+1))) {
					// negative number
					tr.mergeWithNext();
				}
				len++;
				if(!CompileUtil.isOperator(tr.peek(len))) {
					break;
				}
				len++;
			}
			tr = tr.nextAsReader(len);
		}
		tr = TokenReader.tokenize(opPrecedenceParser.parse(tr));
		Operation op = compilePrecedendedOperation(tr);
		return groupOperation ? new GroupOperation(op) : op;
	}

	private Operation compilePrecedendedOperation(TokenReader tr) throws CompilationException {
		if(logger.isDebugEnabled()) logger.debug("compilePrecedendedOperation " + tr);
		String t = tr.next();
		Operation left;
		if("(".equals(t)) {
			TokenReader block = CompileUtil.readGroup(tr, "(", ")");
			left = new GroupOperation(compilePrecedendedOperation(block));
		} else if('"' == t.charAt(0)) {
			left = new ConstantOperation(new Variable().set(t.substring(1, t.length()-1)));
		} else if(CompileUtil.isValue(t)) {
			if(CompileUtil.isBoolean(t)) {
				left = new ConstantOperation(new Variable().set(Boolean.parseBoolean(t)));
			} else if(CompileUtil.isInteger(t)) {
				left = new ConstantOperation(new Variable().set(Long.parseLong(t)));
			} else {
				left = new ConstantOperation(new Variable().set(Double.parseDouble(t)));
			}
		} else if(CompileUtil.isAlphanumeric(t)) {
			left = new ReturnOperation(t);
		} else if("-".equals(t)) {
			t = tr.next();
			if(CompileUtil.isInteger(t)) {
				left = new ConstantOperation(new Variable().set(0-Long.parseLong(t)));
			} else {
				left = new ConstantOperation(new Variable().set(0-Double.parseDouble(t)));
			}
		} else {
			throw new IllegalArgumentException(t); // FIXME
		}
		
		String op = tr.next();
		if("==".equals(op)) {
			return new EqualsOperation(left, compileNext(tr));
		} else if("!=".equals(op)) {
			return new BangOperation(new EqualsOperation(left, compileNext(tr)));
		} else if("<".equals(op)) {
			return new LessThanOperation(left, compileNext(tr));
		} else if("<=".equals(op)) {
			return new LessThanOrEqualToOperation(left, compileNext(tr));
		} else if(">".equals(op)) {
			return new GreaterThanOperation(left, compileNext(tr));
		} else if(">=".equals(op)) {
			return new GreaterThanOrEqualToOperation(left, compileNext(tr));
		} else if("&&".equals(op)) {
			return new AndOperation(left, compileNext(tr));
		} else if("||".equals(op)) {
			return new OrOperation(left, compileNext(tr));
		} else if("+".equals(op)) {
			return new AddOperation(left, compileNext(tr));
		} else if("-".equals(op)) {
			return new SubtractOperation(left, compileNext(tr));
		} else if("*".equals(op)) {
			return new MultiplyOperation(left, compileNext(tr));
		} else if("/".equals(op)) {
			return new DivideOperation(left, compileNext(tr));
		} else if("%".equals(op)) {
			return new ModuloOperation(left, compileNext(tr));
		} else {
			return left;
		}
	}
	
	private void compileFunction(TokenReader tr) throws CompilationException {
		String functionName = tr.next();
		if(logger.isDebugEnabled()) logger.debug("compileFunction " + functionName + " " + tr);
		
		List<String> parameters = new ArrayList<>();
		if("(".equals(tr.peek())) {
			tr.next();
			String t = tr.next();
			while(!")".equals(t)) {
				if(!CompileUtil.isAlphanumeric(t)) {
					throw new CompilationException("Illegal token in function " + functionName + " parameters: " + t);
				}
				parameters.add(t);
				if(",".equals(tr.peek())) {
					tr.next();
				}
				t = tr.next();
			}
		}
		
		if(!"{".equals(tr.peek())) {
			throw new CompilationException("function " + functionName + " body does not start with a '{'");
		}
		
		Operation op = compileNext(tr);
		functionManager.addFunction(functionName, parameters.size(), new Function(functionName, parameters, op)); 
	}
}
