package rook.api.transport.aeron;

import rook.api.config.Configurable;

/**
 * Parsed configuration for an {@link AeronTransport}
 * 
 * @author Eric Thill
 *
 */
public class AeronTransportConfig {
	public static final String DEFAULT_DIRECTORY_NAME = "/dev/shm/rook";
	public static final String DEFAULT_CHANNEL = "aeron:ipc";
	public static final int DEFAULT_STREAM_ID = 1;
	public static final int DEFAULT_SUBSCRIBER_FRAGMENT_LIMIT = 10;
	
	@Configurable(comment="Aeron Directory Name", defaultValue="/dev/shm/rook")
	private String aeronDirectoryName = DEFAULT_DIRECTORY_NAME;
	@Configurable(comment="Aeron Channel", defaultValue="aeron:ipc")
	private String channel = DEFAULT_CHANNEL;
	@Configurable(comment="Aeron Stream ID", defaultValue="1")
	private int streamId = DEFAULT_STREAM_ID;
	@Configurable(comment="Subscriber Fragment Limit", defaultValue="10")
	private int subscriberFragmentLimit = DEFAULT_SUBSCRIBER_FRAGMENT_LIMIT;
	
	public String getAeronDirectoryName() {
		return aeronDirectoryName;
	}
	
	public AeronTransportConfig setAeronDirectoryName(String aeronDirectoryName) {
		this.aeronDirectoryName = aeronDirectoryName;
		return this;
	}
	
	public String getChannel() {
		return channel;
	}
	
	public AeronTransportConfig setChannel(String channel) {
		this.channel = channel;
		return this;
	}
	
	public int getStreamId() {
		return streamId;
	}
	
	public AeronTransportConfig setStreamId(int streamId) {
		this.streamId = streamId;
		return this;
	}
	
	public int getSubscriberFragmentLimit() {
		return subscriberFragmentLimit;
	}
	
	public AeronTransportConfig setSubscriberFragmentLimit(int subscriberFragmentLimit) {
		this.subscriberFragmentLimit = subscriberFragmentLimit;
		return this;
	}
}
