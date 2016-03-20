package rook.core.io.proxy.message;

import rook.api.RID;
import rook.api.transport.GrowableBuffer;
import rook.api.util.BufferUtil;
import uk.co.real_logic.agrona.MutableDirectBuffer;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

/**
 * A single value backed by a {@link GrowableBuffer} 
 * 
 * @author Eric Thill
 *
 */
public class IOValue {

	private static final byte FALSE = 0;
	private static final byte TRUE = 1;
	
	private final RID id;
	private final GrowableBuffer value;
	private final MutableDirectBuffer directBuffer = new UnsafeBuffer(BufferUtil.EMPTY_BUFFER);
	private PrimitiveType type = PrimitiveType.OPAQUE;
	
	public IOValue() {
		this(8);
	}
	
	public IOValue(int defaultCapacity) {
		this.id = RID.create(0);
		this.value = GrowableBuffer.allocate(defaultCapacity);
	}
	
	public IOValue(RID id, boolean value) {
		this.id = id;
		this.value = GrowableBuffer.allocate(1);
		setValue(value);
		this.type = PrimitiveType.BOOLEAN;
	}
	
	public IOValue(RID id, int value) {
		this.id = id;
		this.value = GrowableBuffer.allocate(4);
		setValue(value);
		this.type = PrimitiveType.TWOS_COMPLIMENT_INT;
	}
	
	public IOValue(RID id, long value) {
		this.id = id;
		this.value = GrowableBuffer.allocate(8);
		setValue(value);
		this.type = PrimitiveType.TWOS_COMPLIMENT_INT;
	}
	
	public IOValue(RID id, float value) {
		this.id = id;
		this.value = GrowableBuffer.allocate(4);
		setValue(value);
		this.type = PrimitiveType.IEEE_754_FLOAT;
	}
	
	public IOValue(RID id, double value) {
		this.id = id;
		this.value = GrowableBuffer.allocate(8);
		setValue(value);
		this.type = PrimitiveType.IEEE_754_FLOAT;
	}

	public IOValue(RID id, GrowableBuffer value) {
		this(id, value, PrimitiveType.OPAQUE);
	}
	
	public IOValue(RID id, GrowableBuffer value, PrimitiveType type) {
		this.id = id;
		this.value = GrowableBuffer.allocate(value.getLength());
		setValue(value);
		this.type = type;
	}
	
	public IOValue copy() {
		return new IOValue(id, value, type);
	}
	
	public void copyFrom(IOValue src) {
		this.id.setValue(src.id.toValue());
		this.value.copyFrom(src.value);
		this.directBuffer.wrap(this.value.getBytes());
		this.type = src.type;
	}
	
	public RID getID() {
		return id;
	}
	
	public IOValue setID(RID id) {
		this.id.setValue(id.toValue());
		return this;
	}
	
	public PrimitiveType getType() {
		return type;
	}
	
	public IOValue setValue(boolean value) {
		this.value.reserve(1, false);
		directBuffer.wrap(this.value.getBytes());
		directBuffer.putByte(0, value ? TRUE : FALSE);
		this.value.setLength(4);
		this.type = PrimitiveType.BOOLEAN;
		return this;
	}
	
	public IOValue setValue(int value) {
		this.value.reserve(4, false);
		directBuffer.wrap(this.value.getBytes());
		directBuffer.putInt(0, value);
		this.value.setLength(4);
		this.type = PrimitiveType.TWOS_COMPLIMENT_INT;
		return this;
	}
	
	public IOValue setValue(long value) {
		this.value.reserve(8, false);
		directBuffer.wrap(this.value.getBytes());
		directBuffer.putLong(0, value);
		this.value.setLength(8);
		this.type = PrimitiveType.TWOS_COMPLIMENT_INT;
		return this;
	}
	
	public IOValue setValue(float value) {
		this.value.reserve(4, false);
		directBuffer.wrap(this.value.getBytes());
		directBuffer.putFloat(0, value);
		this.value.setLength(4);
		this.type = PrimitiveType.IEEE_754_FLOAT;
		return this;
	}
	
	public IOValue setValue(double value) {
		this.value.reserve(8, false);
		directBuffer.wrap(this.value.getBytes());
		directBuffer.putDouble(0, value);
		this.value.setLength(8);
		this.type = PrimitiveType.IEEE_754_FLOAT;
		return this;
	}
	
	public IOValue setValue(GrowableBuffer value) {
		this.value.reserve(value.getLength(), false);
		directBuffer.wrap(this.value.getBytes());
		System.arraycopy(value.getBytes(), 0, this.value.getBytes(), 0, value.getLength());
		this.value.setLength(value.getLength());
		this.type = PrimitiveType.OPAQUE;
		return this;
	}
	
	public GrowableBuffer getValue() {
		return value;
	}
	
	public boolean getValueAsBoolean() {
		for(int i = 0; i < value.getLength(); i++) {
			if(value.getBytes()[i] != 0) {
				return true;
			}
		}
		return false;
	}
	
	public byte getValueAsByte() {
		return (byte)getValueAsLong();
	}
	
	public short getValueAsShort() {
		return (short)getValueAsLong();
	}
	
	public int getValueAsInt() {
		return (int)getValueAsLong();
	}
	
	public long getValueAsLong() {
		if(type == PrimitiveType.BOOLEAN) {
			return getValueAsBoolean() ? 1 : 0;
		} else if(type == PrimitiveType.IEEE_754_FLOAT) {
			return (long)getValueAsDouble();
		} else if(type == PrimitiveType.TWOS_COMPLIMENT_INT) {
			if(value.getLength() == 8) {
				return directBuffer.getLong(0);
			} else if(value.getLength() == 4) {
				return directBuffer.getInt(0);
			} else if(value.getLength() == 2) {
				return directBuffer.getShort(0);
			} else if(value.getLength() == 1) {
				return directBuffer.getByte(0);
			} else {
				throw new NumberFormatException(value.toString());
			}
		} else {
			throw new NumberFormatException("Not a number. type=" + type + " value=" + value.toString());
		}
	}
	
	public float getValueAsFloat() {
		return (float)getValueAsDouble();
	}
	
	public double getValueAsDouble() {
		if(type == PrimitiveType.BOOLEAN) {
			return getValueAsBoolean() ? 1 : 0;
		} else if(type == PrimitiveType.TWOS_COMPLIMENT_INT) {
			return (double)getValueAsLong();
		} else if(type == PrimitiveType.IEEE_754_FLOAT) {
			if(value.getLength() == 8) {
				return directBuffer.getDouble(0);
			} else if(value.getLength() == 4) {
				return directBuffer.getFloat(0);
			} else {
				throw new NumberFormatException(value.toString());
			}
		} else {
			throw new NumberFormatException("Not a number. type=" + type + " value=" + value.toString());
		}
	}
	
	public Number getValueAsNumber() {
		if(type == PrimitiveType.BOOLEAN) {
			return getValueAsBoolean() ? 1 : 0;
		} else if(type == PrimitiveType.IEEE_754_FLOAT) {
			return getValueAsDouble();
		} else if(type == PrimitiveType.TWOS_COMPLIMENT_INT) {
			return getValueAsLong();
		} else {
			throw new NumberFormatException("Not a number. type=" + type + " value=" + value.toString());
		}
	}
	
	public String getValueAsString() {
		if(type == PrimitiveType.BOOLEAN) {
			return Boolean.toString(getValueAsBoolean());
		} else if(type == PrimitiveType.TWOS_COMPLIMENT_INT) {
			return Long.toString(getValueAsLong());
		} else if(type == PrimitiveType.IEEE_754_FLOAT) {
			return Double.toString(getValueAsDouble());
		} else if(type == PrimitiveType.UTF_8) {
			return directBuffer.getStringWithoutLengthUtf8(0, value.getLength());
		} else {
			return value.toString();
		}
		
	}
	
	public int deserialize(GrowableBuffer buffer, int off) {
		directBuffer.wrap(buffer.getBytes());
		id.setValue(directBuffer.getLong(off));
		off += 8;
		type = PrimitiveType.fromValue(directBuffer.getByte(off));
		off += 1;
		int len = directBuffer.getInt(off);
		off += 4;
		value.setLength(0);
		value.reserve(len, false);
		value.put(buffer.getBytes(), off, len);
		directBuffer.wrap(value.getBytes());
		return getSerializedLength();
	}
	
	public void serialize(GrowableBuffer buffer) {
		buffer.reserve(buffer.getLength()+getSerializedLength(), true);
		directBuffer.wrap(buffer.getBytes());
		int off = buffer.getLength();
		directBuffer.putLong(off, id.toValue());
		off += 8;
		directBuffer.putByte(off, type.getValue());
		off += 1;
		directBuffer.putInt(off, value.getLength());
		off += 4;
		directBuffer.putBytes(off, value.getBytes(), 0, value.getLength());
		off += value.getLength();
		buffer.setLength(off);
		directBuffer.wrap(value.getBytes());
	}
	
	public int getSerializedLength() {
		return 1 + 4 + 8 + value.getLength();
	}
	
	@Override
	public String toString() {
		return "IOValue [id=" + id + ", value=" + getValueAsString() + "]";
	}
	
}
