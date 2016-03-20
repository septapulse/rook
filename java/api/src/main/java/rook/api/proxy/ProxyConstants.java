package rook.api.proxy;

/**
 * Constants used by a {@link ProxyService}
 * 
 * @author Eric Thill
 *
 */
public final class ProxyConstants {
	private ProxyConstants() { }
	public static final byte TYPE_ANNOUNCEMENT = 0;
	public static final byte TYPE_BROADCAST_JOIN = 1;
	public static final byte TYPE_BROADCAST_LEAVE = 2;
	public static final byte TYPE_BROADCAST_MESSAGE = 3;
	public static final byte TYPE_UNICAST_MESSAGE = 4;
	public static final byte TYPE_RESEND_REQUEST = 5;
}
