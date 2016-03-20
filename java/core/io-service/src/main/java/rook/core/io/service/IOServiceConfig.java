package rook.core.io.service;

import rook.api.config.ConfigurableInteger;

/**
 * Base configuration needed for an {@link IOService}
 * 
 * @author Eric Thill
 *
 */
public class IOServiceConfig {
	@ConfigurableInteger(min=1, max=Long.MAX_VALUE, increment=1, 
			comment="Number of milliseconds between broadcasting IOInput changes")
	private long broadcastInterval;
	
	@ConfigurableInteger(min=1, max=Long.MAX_VALUE, increment=1, 
			comment="When disconnected, the number of milliseconds between reconnect attempts")
	private long reconnectInterval;
	
	public final long getBroadcastInterval() {
		return broadcastInterval;
	}
	
	public final long getReconnectInterval() {
		return reconnectInterval;
	}
}
