package rook.api.lang;

/**
 * 
 * 
 * @author Eric Thill
 *
 */
public class Variable extends Number implements Comparable<Variable> {
	
	private static final long serialVersionUID = 2530506490968659999L;

	public enum Type {
		LONG, DOUBLE, STRING;
	}
	
	private Type type;
	private long longVal;
	private double doubleVal;
	private String stringVal;
	
	public Variable copyFrom(Variable o) {
		type = o.type;
		longVal = o.longVal;
		doubleVal = o.doubleVal;
		stringVal = o.stringVal;
		return this;
	}

	public Variable set(boolean value) {
		set(value ? 1L : 0L);
		return this;
	}
	
	public Variable set(short value) {
		set((long)value);
		return this;
	}
	
	public Variable set(int value) {
		set((long)value);
		return this;
	}
	
	public Variable set(long value) {
		longVal = value;
		type = Type.LONG;
		stringVal = null;
		return this;
	}
	
	public Variable set(float value) {
		set((double)value);
		return this;
	}

	public Variable set(double value) {
		doubleVal = value;
		type = Type.DOUBLE;
		stringVal = null;
		return this;
	}
	
	public Variable set(String value) {
		stringVal = value;
		type = Type.STRING;
		return this;
	}
	
	@Override
	public int intValue() {
		return (int)longValue();
	}

	@Override
	public long longValue() {
		if(type == Type.LONG) {
			return longVal;
		} else if(type == Type.DOUBLE) {
			return (long)doubleVal;
		} else {
			return Long.parseLong(stringVal);
		}
	}

	@Override
	public float floatValue() {
		return (float)doubleVal;
	}

	@Override
	public double doubleValue() {
		if(type == Type.LONG) {
			return longVal;
		} else if(type == Type.DOUBLE) {
			return doubleVal;
		} else {
			return Double.parseDouble(stringVal);
		}
	}
	
	public boolean booleanValue() {
		if(type == Type.LONG) {
			return longVal != 0L;
		} else if(type == Type.DOUBLE) {
			return doubleVal != 0d;
		} else {
			return Boolean.parseBoolean(stringVal);
		}
	}
	
	@Override
	public int compareTo(Variable o) {
		if(o.type == Type.STRING || type == Type.STRING) {
			return toString().compareTo(o.toString());
		} else if(o.type == Type.LONG && type == Type.LONG){
			return Long.compare(longValue(), o.longValue());
		} else {
			return Double.compare(doubleValue(), o.doubleValue());
		}
	}
	
	@Override
	public int hashCode() {
		String val = toString();
		final int prime = 31;
		int result = 1;
		result = prime * result + ((val == null) ? 0 : val.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Variable other = (Variable) obj;
		if(other.type == this.type) {
			if(type == Type.LONG) {
				return longVal == other.longVal;
			} else if(type == Type.DOUBLE) {
				return doubleVal == other.doubleVal;
			}
		}
		return toString().equals(other.toString());
	}

	@Override
	public String toString() {
		if(type == Type.LONG) {
			return Long.toString(longVal);
		} else if(type == Type.DOUBLE) {
			return Double.toString(doubleVal);
		} else {
			return stringVal;
		}
	}

	public void add(Variable v2) {
		if(type == Type.STRING || v2.type == Type.STRING) {
			stringVal = toString() + v2.toString();
			type = Type.STRING;
		}
		
//		convertToNumber();
//		v2.convertToNumber();
		if(type == Type.LONG) {
			if(v2.type == Type.LONG) {
				longVal += v2.longValue();
			} else {
				set(doubleValue());
				doubleVal += v2.doubleValue();
			}
		} else if(type == Type.DOUBLE) {
			doubleVal += v2.doubleValue();
		}
	}
	
	public void subtract(Variable v2) {
		convertToNumber();
		v2.convertToNumber();
		if(type == Type.LONG) {
			if(v2.type == Type.LONG) {
				longVal -= v2.longValue();
			} else {
				set(doubleValue());
				doubleVal -= v2.doubleValue();
			}
		} else if(type == Type.DOUBLE) {
			doubleVal -= v2.doubleValue();
		}
	}
	
	public void multiply(Variable v2) {
		convertToNumber();
		v2.convertToNumber();
		if(type == Type.LONG) {
			if(v2.type == Type.LONG) {
				longVal *= v2.longValue();
			} else {
				set(doubleValue());
				doubleVal *= v2.doubleValue();
			}
		} else if(type == Type.DOUBLE) {
			doubleVal *= v2.doubleValue();
		}
	}
	
	public void divide(Variable v2) {
		convertToNumber();
		v2.convertToNumber();
		if(type == Type.LONG) {
			if(v2.type == Type.LONG) {
				longVal /= v2.longValue();
			} else {
				set(doubleValue());
				doubleVal /= v2.doubleValue();
			}
		} else if(type == Type.DOUBLE) {
			doubleVal /= v2.doubleValue();
		}
	}
	
	public void modulo(Variable v2) {
		convertToNumber();
		v2.convertToNumber();
		if(type == Type.LONG) {
			if(v2.type == Type.LONG) {
				longVal %= v2.longValue();
			} else {
				set(doubleValue());
				doubleVal %= v2.doubleValue();
			}
		} else if(type == Type.DOUBLE) {
			doubleVal %= v2.doubleValue();
		}
	}
	
	private void convertToNumber() {
		if(type == Type.STRING) {
			if(isInteger(stringVal)) {
				set(longValue());
			} else {
				set(doubleValue());
			}
		}
	}
	
	private static boolean isInteger(String s) {
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}
	
	public Type type() {
		return type;
	}

}
