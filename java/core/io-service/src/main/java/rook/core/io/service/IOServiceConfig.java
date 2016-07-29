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
			comment="Number of milliseconds between broadcasting IOInput changes",
			defaultValue="100")
	private long broadcastInterval = 100;
	
	@ConfigurableInteger(min=1, max=Long.MAX_VALUE, increment=1, 
			comment="When disconnected, the number of milliseconds between reconnect attempts",
			defaultValue="1000")
	private long reconnectInterval = 1000;
	
	public final long getBroadcastInterval() {
		return broadcastInterval;
	}
	
	public IOServiceConfig setBroadcastInterval(long broadcastInterval) {
		this.broadcastInterval = broadcastInterval;
		return this;
	}
	
	public final long getReconnectInterval() {
		return reconnectInterval;
	}
	
	public IOServiceConfig setReconnectInterval(long reconnectInterval) {
		this.reconnectInterval = reconnectInterval;
		return this;
	}
}
