package rook.core.io.proxy.message;

import org.junit.Assert;
import org.junit.Test;

import rook.api.transport.GrowableBuffer;

public class TestIOValue {

	@Test
	public void testLong() {
		IOValue v = new IOValue();
		v.setValue(1234567890123456L);
		Assert.assertEquals(1234567890123456L, v.getValueAsLong());
	}
	
	@Test
	public void testDouble() {
		IOValue v = new IOValue();
		v.setValue(1235235.2359484);
		Assert.assertEquals(1235235.2359484, v.getValueAsDouble(), 0);
	}
	
	@Test
	public void testSerializeLong() {
		IOValue v = new IOValue();
		v.setValue(1234567890123456L);
		
		int off = 10;
		GrowableBuffer serialized = GrowableBuffer.allocate(10);
		serialized.length(10); // force off != 0 and buffer growth
		v.serialize(serialized);
		
		IOValue deserialized = new IOValue();
		deserialized.deserialize(serialized, off);
		
		Assert.assertEquals(1234567890123456L, deserialized.getValueAsLong());
	}
}
