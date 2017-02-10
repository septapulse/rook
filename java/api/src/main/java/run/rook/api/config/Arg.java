package run.rook.api.config;

/**
 * Used by {@link Args} to dermine the format of the given arguments.
 * 
 * @author Eric Thill
 *
 */
public class Arg {

	public static Arg arg(String flag, String name, boolean hasValue, boolean required, boolean repeating,
			String description) {
		return new Arg(flag, name, hasValue, required, repeating, description);
	}

	private final String flag;
	private final String name;
	private final boolean hasValue;
	private final boolean required;
	private final boolean repeating;
	private final String description;

	/**
	 * Constructor
	 * 
	 * @param flag
	 *            short-hand name
	 * @param name
	 *            long-hand name
	 * @param hasValue
	 *            if followed by an associated value
	 * @param required
	 *            if it is required
	 * @param repeating
	 *            if it is allowed to have multiple values
	 * @param description
	 *            a description of the field
	 */
	public Arg(String flag, String name, boolean hasValue, boolean required, boolean repeating, String description) {
		this.flag = flag;
		this.name = name;
		this.hasValue = hasValue;
		this.required = required;
		this.repeating = repeating;
		this.description = description;
	}

	public String getFlag() {
		return flag;
	}

	public String getName() {
		return name;
	}

	public boolean isHasValue() {
		return hasValue;
	}

	public boolean isRequired() {
		return required;
	}

	public boolean isRepeating() {
		return repeating;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return "-" + flag + " --" + name + "  hasValue=" + hasValue + ", required=" + required + " repeating="
				+ repeating + " description: " + description;
	}

}
