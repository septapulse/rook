package rook.core.io.proxy.message;

import rook.api.RID;
import rook.api.transport.GrowableBuffer;
import rook.api.util.BufferUtil;
import uk.co.real_logic.agrona.MutableDirectBuffer;
import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

/**
 * A capabilit that represents a single input or output
 * 
 * @author Eric Thill
 *
 */
public class Cap {
	private final MutableDirectBuffer directBuffer = new UnsafeBuffer(BufferUtil.EMPTY_BUFFER);
	private final RID id = new RID();
	private CapType capType;
	private DataType dataType;
	private double minValue;
	private double maxValue;
	private double increment;
	
	public RID getId() {
		return id;
	}
	
	public CapType getCapType() {
		return capType;
	}
	
	public DataType getDataType() {
		return dataType;
	}
	
	public double getMinValue() {
		return minValue;
	}
	
	public double getMaxValue() {
		return maxValue;
	}
	
	public double getIncrement() {
		return increment;
	}
	
	public Cap setID(RID id) {
		this.id.copyFrom(id);
		return this;
	}
	
	public Cap setCapType(CapType capType) {
		this.capType = capType;
		return this;
	}
	
	public Cap setDataType(DataType dataType) {
		this.dataType = dataType;
		return this;
	}
	
	public Cap setMinValue(double minValue) {
		this.minValue = minValue;
		return this;
	}
	
	public Cap setMaxValue(double maxValue) {
		this.maxValue = maxValue;
		return this;
	}
	
	public Cap setIncrement(double increment) {
		this.increment = increment;
		return this;
	}

	@Override
	public String toString() {
		return "Cap [id=" + id + ", capType=" + capType + ", dataType=" + dataType + ", minValue=" + minValue
				+ ", maxValue=" + maxValue + ", increment=" + increment + "]";
	}
	
	public int deserialize(GrowableBuffer buffer, int off) {
		directBuffer.wrap(buffer.getBytes());
		id.setValue(directBuffer.getLong(off));
		off+=8;
		capType = CapType.fromValue(directBuffer.getByte(off));
		off++;
		dataType = DataType.fromValue(directBuffer.getByte(off));
		off++;
		if(dataType == DataType.INTEGER) {
			minValue = directBuffer.getLong(off);
			off+=8;
			maxValue = directBuffer.getLong(off);
			off+=8;
			increment = directBuffer.getLong(off);
			off+=8;
		} else if(dataType == DataType.FLOAT) {
			minValue = directBuffer.getDouble(off);
			off+=8;
			maxValue = directBuffer.getDouble(off);
			off+=8;
			increment = directBuffer.getDouble(off);
			off+=8;
		}
		
		return getSerializedSize();
	}
	
	public void serialize(GrowableBuffer buffer) {
		int off = buffer.getLength();
		buffer.reserve(off+getSerializedSize(), true);
		directBuffer.wrap(buffer.getBytes());
		
		directBuffer.putLong(off, id.toValue());
		off+=8;
		directBuffer.putByte(off, capType.getValue());
		off++;
		directBuffer.putByte(off, dataType.getValue());
		off++;
		if(dataType == DataType.INTEGER) {
			directBuffer.putLong(off, (long)minValue);
			off+=8;
			directBuffer.putLong(off, (long)maxValue);
			off+=8;
			directBuffer.putLong(off, (long)increment);
			off+=8;
		} else if(dataType == DataType.FLOAT) {
			directBuffer.putDouble(off, minValue);
			off+=8;
			directBuffer.putDouble(off, maxValue);
			off+=8;
			directBuffer.putDouble(off, increment);
			off+=8;
		}
		
		buffer.setLength(off);
	}
	
	public int getSerializedSize() {
		int size = 10;
		if(dataType == DataType.FLOAT || dataType == DataType.INTEGER) {
			size += 24;
		}
		return size;
	}
	
}
