package rook.cli.message.pkg;

import com.google.gson.Gson;

public class ArgumentInfo {
	private String name;
	private ArgumentType type;
	private Double minValue;
	private Double maxValue;
	private Double increment;

	public String getName() {
		return name;
	}

	public ArgumentInfo setName(String name) {
		this.name = name;
		return this;
	}

	public ArgumentType getType() {
		return type;
	}

	public ArgumentInfo setType(ArgumentType type) {
		this.type = type;
		return this;
	}

	public Double getMinValue() {
		return minValue;
	}

	public ArgumentInfo setMinValue(Double minValue) {
		this.minValue = minValue;
		return this;
	}

	public Double getMaxValue() {
		return maxValue;
	}

	public ArgumentInfo setMaxValue(Double maxValue) {
		this.maxValue = maxValue;
		return this;
	}

	public Double getIncrement() {
		return increment;
	}

	public ArgumentInfo setIncrement(Double increment) {
		this.increment = increment;
		return this;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
}
