package rook.core.io.proxy.message;

import rook.api.RID;
import rook.api.transport.GrowableBuffer;

/**
 * A capabilit that represents a single input or output
 * 
 * @author Eric Thill
 *
 */
public class Cap {
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
		id.setValue(buffer.direct().getLong(off));
		off+=8;
		capType = CapType.fromValue(buffer.direct().getByte(off));
		off++;
		dataType = DataType.fromValue(buffer.direct().getByte(off));
		off++;
		if(dataType == DataType.INTEGER) {
			minValue = buffer.direct().getLong(off);
			off+=8;
			maxValue = buffer.direct().getLong(off);
			off+=8;
			increment = buffer.direct().getLong(off);
			off+=8;
		} else if(dataType == DataType.FLOAT) {
			minValue = Double.longBitsToDouble(buffer.direct().getLong(off));
			off+=8;
			maxValue = Double.longBitsToDouble(buffer.direct().getLong(off));
			off+=8;
			increment = Double.longBitsToDouble(buffer.direct().getLong(off));
			off+=8;
		}
		
		return getSerializedSize();
	}
	
	public void serialize(GrowableBuffer buffer) {
		int off = buffer.length();
		buffer.reserve(off+getSerializedSize(), true);
		
		buffer.direct().putLong(off, id.toValue());
		off+=8;
		buffer.direct().putByte(off, capType.getValue());
		off++;
		buffer.direct().putByte(off, dataType.getValue());
		off++;
		if(dataType == DataType.INTEGER) {
			buffer.direct().putLong(off, (long)minValue);
			off+=8;
			buffer.direct().putLong(off, (long)maxValue);
			off+=8;
			buffer.direct().putLong(off, (long)increment);
			off+=8;
		} else if(dataType == DataType.FLOAT) {
			buffer.direct().putLong(off, Double.doubleToLongBits(minValue));
			off+=8;
			buffer.direct().putLong(off, Double.doubleToLongBits(maxValue));
			off+=8;
			buffer.direct().putLong(off, Double.doubleToLongBits(increment));
			off+=8;
		}
		
		buffer.length(off);
	}
	
	public int getSerializedSize() {
		int size = 10;
		if(dataType == DataType.FLOAT || dataType == DataType.INTEGER) {
			size += 24;
		}
		return size;
	}
	
}
