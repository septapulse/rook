package io.septapulse.rook.core.io.proxy.message;

import io.septapulse.rook.api.transport.GrowableBuffer;

/**
 * A single value backed by a {@link GrowableBuffer} 
 * 
 * @author Eric Thill
 *
 */
public class IOValue {

	private static final byte FALSE = 0;
	private static final byte TRUE = 1;
	
	private final GrowableBuffer value;
	private PrimitiveType type = PrimitiveType.OPAQUE;
	
	public IOValue() {
		this.value = GrowableBuffer.allocate(0);
	}
	
	public IOValue(boolean value) {
		this.value = GrowableBuffer.allocate(1);
		setValue(value);
		this.type = PrimitiveType.BOOLEAN;
	}
	
	public IOValue(int value) {
		this.value = GrowableBuffer.allocate(4);
		setValue(value);
		this.type = PrimitiveType.TWOS_COMPLIMENT_INT;
	}
	
	public IOValue(long value) {
		this.value = GrowableBuffer.allocate(8);
		setValue(value);
		this.type = PrimitiveType.TWOS_COMPLIMENT_INT;
	}
	
	public IOValue(float value) {
		this.value = GrowableBuffer.allocate(4);
		setValue(value);
		this.type = PrimitiveType.IEEE_754_FLOAT;
	}
	
	public IOValue(double value) {
		this.value = GrowableBuffer.allocate(8);
		setValue(value);
		this.type = PrimitiveType.IEEE_754_FLOAT;
	}

	public IOValue(GrowableBuffer value) {
		this(value, PrimitiveType.OPAQUE);
	}
	
	public IOValue(GrowableBuffer value, PrimitiveType type) {
		this.value = GrowableBuffer.allocate(value.length());
		setValue(value);
		this.type = type;
	}
	
	public IOValue copy() {
		return new IOValue(value, type);
	}
	
	public void copyFrom(IOValue src) {
		this.value.copyFrom(src.value);
		this.type = src.type;
	}
	
	public PrimitiveType getType() {
		return type;
	}
	
	public IOValue setValue(boolean value) {
		this.value.reserve(1, false);
		this.value.direct().putByte(0, value ? TRUE : FALSE);
		this.value.length(1);
		this.type = PrimitiveType.BOOLEAN;
		return this;
	}
	
	public IOValue setValue(int value) {
		this.value.reserve(4, false);
		this.value.direct().putInt(0, value);
		this.value.length(4);
		this.type = PrimitiveType.TWOS_COMPLIMENT_INT;
		return this;
	}
	
	public IOValue setValue(long value) {
		this.value.reserve(8, false);
		this.value.direct().putLong(0, value);
		this.value.length(8);
		this.type = PrimitiveType.TWOS_COMPLIMENT_INT;
		return this;
	}
	
	public IOValue setValue(float value) {
		this.value.reserve(4, false);
		this.value.direct().putInt(0, Float.floatToIntBits(value));
		this.value.length(4);
		this.type = PrimitiveType.IEEE_754_FLOAT;
		return this;
	}
	
	public IOValue setValue(double value) {
		this.value.reserve(8, false);
		this.value.direct().putLong(0, Double.doubleToLongBits(value));
		this.value.length(8);
		this.type = PrimitiveType.IEEE_754_FLOAT;
		return this;
	}
	
	public IOValue setValue(GrowableBuffer value) {
		this.value.copyFrom(value);
		this.type = PrimitiveType.OPAQUE;
		return this;
	}
	
	public GrowableBuffer getValue() {
		return value;
	}
	
	public boolean getValueAsBoolean() {
		for(int i = 0; i < value.length(); i++) {
			if(value.bytes()[i] != 0) {
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
			if(value.length() == 8) {
				return this.value.direct().getLong(0);
			} else if(value.length() == 4) {
				return this.value.direct().getInt(0);
			} else if(value.length() == 2) {
				return this.value.direct().getShort(0);
			} else if(value.length() == 1) {
				return this.value.direct().getByte(0);
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
			if(value.length() == 8) {
				return Double.longBitsToDouble(this.value.direct().getLong(0));
			} else if(value.length() == 4) {
				return Float.intBitsToFloat(this.value.direct().getInt(0));
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
			return this.value.direct().getStringWithoutLengthUtf8(0, value.length());
		} else {
			return value.toString();
		}
		
	}
	
	public int deserialize(GrowableBuffer buffer, int off) {
//		id.setValue(buffer.direct().getLong(off));
//		off += 8;
		type = PrimitiveType.fromValue(buffer.direct().getByte(off));
		off += 1;
		int len = buffer.direct().getInt(off);
		off += 4;
		value.length(0);
		value.reserve(len, false);
		value.direct().putBytes(0, buffer.bytes(), off, len);
		value.length(len);
		return getSerializedLength();
	}
	
	public void serialize(GrowableBuffer buffer) {
		buffer.reserve(buffer.length()+getSerializedLength(), true);
		int off = buffer.length();
//		buffer.direct().putLong(off, id.toValue());
//		off += 8;
		buffer.direct().putByte(off, type.getValue());
		off += 1;
		buffer.direct().putInt(off, value.length());
		off += 4;
		buffer.direct().putBytes(off, value.bytes(), 0, value.length());
		off += value.length();
		buffer.length(off);
	}
	
	public int getSerializedLength() {
		return 1 + 4 + value.length();
	}
	
	@Override
	public String toString() {
		return getValueAsString();
	}
	
}
