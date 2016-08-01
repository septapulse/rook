package rook.ui.websocket.message;

public class TemplateField {
	private String name;
	private String defaultValue;
	private String comment;
	private String min;
	private String max;
	private String increment;
	private boolean array;
	private boolean object;

	public String getName() {
		return name;
	}

	public TemplateField setName(String name) {
		this.name = name;
		return this;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public TemplateField setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	public String getComment() {
		return comment;
	}

	public TemplateField setComment(String comment) {
		this.comment = comment;
		return this;
	}

	public String getMin() {
		return min;
	}

	public TemplateField setMin(String min) {
		this.min = min;
		return this;
	}

	public String getMax() {
		return max;
	}

	public TemplateField setMax(String max) {
		this.max = max;
		return this;
	}

	public String getIncrement() {
		return increment;
	}

	public TemplateField setIncrement(String increment) {
		this.increment = increment;
		return this;
	}
	
	public boolean isArray() {
		return array;
	}
	
	public TemplateField setArray(boolean array) {
		this.array = array;
		return this;
	}
	
	public boolean isObject() {
		return object;
	}
	
	public TemplateField setObject(boolean object) {
		this.object = object;
		return this;
	}
	
}
