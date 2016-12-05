package io.septapulse.rook.api;

/**
 * Represents a Rook ID. This ID is represented as String that is backed by a
 * long. Valid Characters are [0-9][A-Z][_.]. The String must be 12 characters
 * or less. <br>
 * Humans are good at remembering names, and computers are efficient at
 * comparing numbers. RID allows humans to represent an ID with a name, while
 * allowing the computer to do filtering by comparing a single number. It also
 * allows serialization of an ID to fit into a fixed-width 8-byte field.
 * 
 * 
 * @author Eric Thill
 *
 */
public final class RID {

	private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_.";
	private static final int BASE = ALPHABET.length();

	public static RID create(String str) {
		final RID key = new RID();
		key.parse(str);
		return key;
	}

	public static RID create(long value) {
		final RID id = new RID();
		id.parse(value);
		return id;
	}

	private final boolean immutable;
	private long value;
	
	public RID() {
		this(0);
	}

	protected RID(long value) {
		this(value, false);
	}
	
	private RID(long value, boolean immutable) {
		this.immutable = immutable;
		this.value = value;
	}

	public void parse(String str) {
		if(immutable) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		if (str.length() == 0 || str.length() > 12) {
			throw new IllegalArgumentException(
					"'" + str + "' is not a valid value. Can only contain [A-Z][0-9][_.] and be 1 to 12 digits long.");
		}
		this.value = toBase10(str);
	}

	public void parse(long value) {
		this.value = value;
	}

	public long toValue() {
		return value;
	}

	@Override
	public String toString() {
		return fromBase10(value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (value ^ (value >>> 32));
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
		RID other = (RID) obj;
		if (value != other.value)
			return false;
		return true;
	}

	public RID copy() {
		return RID.create(value);
	}

	public RID immutable() {
		if(immutable) {
			return this;
		} else {
			return new RID(value, true);
		}
	}

	public void copyFrom(RID src) {
		if(immutable) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		this.value = src.value;
	}

	public void setValue(long value) {
		if(immutable) {
			throw new UnsupportedOperationException("Unmodifiable");
		}
		this.value = value;
	}

	private static String fromBase10(long i) {
		StringBuilder sb = new StringBuilder(12);
		while (i > 0) {
			int rem = (int) (i % BASE);
			sb.append(ALPHABET.charAt(rem));
			i = i / BASE;
		}
		return sb.reverse().toString();
	}

	private static long toBase10(String str) {
		final int length = str.length();
		long result = 0;
		int curIdx = 0;
		for (int index = 0; index < length; index++) {
			char c = str.charAt(length - index - 1);
			if (c >= 'a' && c <= 'z') {
				// to upper-case
				c -= 'a' - 'A';
			}
			if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || c == '.' || c == '_') {
				// only encode alphanumeric characters
				int digit = ALPHABET.indexOf(c);
				result += digit * pow(BASE, curIdx++);
			}
		}
		return result;
	}

	private static long pow(long n, int pow) {
		long r = 1;
		for (int i = 0; i < pow; i++) {
			r *= n;
		}
		return r;
	}

}
