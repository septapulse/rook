package rook.api.transport;

/**
 * Deserializes a payload as string
 * 
 * @author Eric Thill
 *
 */
public class StringDeserializer implements Deserializer<String> {

	@Override
	public String deserialize(GrowableBuffer msg) {
		if (msg.getLength() == 0) {
			return "";
		}
		return new String(msg.getBytes(), 0, msg.getLength());
	}
}
