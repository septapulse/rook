package rook.ui.environment;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Utility to listen to an environment's log output
 * 
 * @author Eric Thill
 *
 */
public class Log {
	private static final int LOG_CACHE_SIZE = 20;
	private final Set<Consumer<String>> logConsumers = new LinkedHashSet<>();
	private final LinkedList<String> logCache = new LinkedList<>();
	
	public void dispatch(String line) {
		synchronized (logCache) {
			logCache.add(line);
			while(logCache.size() > LOG_CACHE_SIZE) {
				logCache.pop();
			}
		}
		synchronized (logConsumers) {
			Iterator<Consumer<String>> t = logConsumers.iterator();
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
	
	public void clear() {
		synchronized (logCache) {
			logCache.clear();
		}
	}
	
	public void addLogConsumer(Consumer<String> consumer) {
		synchronized (logCache) {
			for(String s : logCache) {
				consumer.accept(s);
			}
		}
		synchronized (logConsumers) {
			logConsumers.add(consumer);
		}
	}

	public void removeLogConsumer(Consumer<String> consumer) {
		synchronized (logConsumers) {
			logConsumers.remove(consumer);
		}
	}
}
