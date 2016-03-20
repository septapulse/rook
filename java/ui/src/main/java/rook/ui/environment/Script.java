package rook.ui.environment;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rook.api.RID;
import rook.api.lang.Application;
import rook.api.lang.CompilationException;
import rook.api.lang.Compiler;
import rook.api.lang.GlobalScope;
import rook.api.lang.Variable;
import rook.api.lang.Variable.Type;
import rook.api.transport.GrowableBuffer;
import rook.core.io.proxy.IOProxy;
import rook.core.io.proxy.message.Cap;
import rook.core.io.proxy.message.IOValue;

/**
 * Utility to compile and run Rook scripts
 * 
 * @author Eric Thill
 *
 */
public class Script {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final GlobalScope scriptScope = new GlobalScope();
	private final Set<Consumer<String>> consoleConsumers = Collections.synchronizedSet(new LinkedHashSet<>());
	private final IOProxy ioProxy;
	private String code = "print(\"HELLO WORLD!\")\n";
	private Application app;
	private volatile ScriptExecutor scriptExecutor;
	
	public Script(IOProxy ioProxy) {
		this.ioProxy = ioProxy;
		ioProxy.inputs().addConsumer(this::handleValue);
		ioProxy.outputs().addConsumer(this::handleValue);
		ioProxy.caps().addConsumer(this::handleCaps);
		scriptScope.addVariableChangeConsumer(this::sendOutput);
	}
	
	public String getCode() {
		return code;
	}
	
	private void sendOutput(CharSequence name, Variable var) {
		// FIXME garbage
		RID id = RID.create(name.toString());
		IOValue v = new IOValue().setID(id);
		if(var.type() == Type.DOUBLE) {
			v.setValue(var.doubleValue());
		} else if(var.type() == Type.LONG) {
			v.setValue(var.longValue());
		} else {
			// FIXME garbage
			String str = var.toString();
			GrowableBuffer buffer = GrowableBuffer.allocate(str.length());
			for(int i = 0; i < str.length(); i++) {
				buffer.getBytes()[i] = (byte)str.charAt(i);
			}
			v.setValue(buffer);
		}
		ioProxy.outputs().setOutput(v);
	}
	
	private void handleCaps(List<Cap> caps) {
		if(caps.size() == 0) {
			scriptScope.clear();
		}
		for(Cap c : caps) {
			String name = c.getId().toString();
			if(scriptScope.getVariable(name) == null) {
				scriptScope.setByReference(name, new Variable().set(0));
			}
		}
	}
	
	private void handleValue(IOValue value) {
		switch(value.getType()) {
		case BOOLEAN:
			getOrCreateVariable(value.getID().toString()).set(value.getValueAsBoolean());
			break;
		case IEEE_754_FLOAT:
			getOrCreateVariable(value.getID().toString()).set(value.getValueAsDouble());
			break;
		case TWOS_COMPLIMENT_INT:
			getOrCreateVariable(value.getID().toString()).set(value.getValueAsLong());
			break;
		case UTF_8:
			getOrCreateVariable(value.getID().toString()).set(value.getValueAsString());
			break;
		default:
			// unsupported
			break;
		}
	}
	
	private Variable getOrCreateVariable(String name) {
		Variable v = scriptScope.getVariable(name);
		if(v == null) {
			v = new Variable();
			scriptScope.setByReference(name, v);
		}
		return v;
	}

	public void save(String text) {
		code = text == null ? "" : text;
	}
	
	public void compile() throws CompilationException {
		stop();
		app = null;
		app = Compiler.compile(code, this::console);
		logger.info("Compiled Script: " + app.toString());
	}
	
	private void console(String line) {
		if(logger.isDebugEnabled()) {
			logger.debug("Script Console: " + line);
		}
		logger.info("Script Console: " + line);
		synchronized (consoleConsumers) {
			Iterator<Consumer<String>> t = consoleConsumers.iterator();
			while(t.hasNext()) {
				Consumer<String> c = t.next();
				try {
					c.accept(line);
				} catch(Throwable e) {
					// remove the bad consumer
					t.remove();
				}
			}
		}
	}
	
	public void start() {
		if(app != null) {
			logger.info("Starting Script");
			scriptExecutor = new ScriptExecutor();
			new Thread(scriptExecutor).start();
		}
	}

	public void stop() {
		if(scriptExecutor != null && scriptExecutor.isRunning()) {
			logger.info("Stopping Script");
			app.stop();
		}
	}
	
	public void addConsoleConsumer(Consumer<String> c) {
		consoleConsumers.add(c);
	}

	public void removeConsoleConsumer(Consumer<String> c) {
		consoleConsumers.add(c);
	}
	
	private class ScriptExecutor implements Runnable {
		private volatile boolean running = false;
		@Override
		public void run() {
			running = true;
			try {
				handleCaps(ioProxy.caps().getCaps());
				app.execute(scriptScope);
			} catch(Throwable t) {
				if(InterruptedException.class != t.getCause().getClass()) {
					// FIXME send to client
					logger.info("Script Execution Error", t);
				}
			}
			logger.info("Script Finished");
			running = false;
		}
		public boolean isRunning() {
			return running;
		}
	}
}
