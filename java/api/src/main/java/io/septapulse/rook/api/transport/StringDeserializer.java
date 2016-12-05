package io.septapulse.rook.api.transport;

/**
 * Deserializes a payload as string
 * 
 * @author Eric Thill
 *
 */
public class StringDeserializer implements Deserializer<String> {

	@Override
	public String deserialize(GrowableBuffer msg) {
		if (msg.length() == 0) {
			return "";
		}
		return new String(msg.bytes(), 0, msg.length());
	}
}
