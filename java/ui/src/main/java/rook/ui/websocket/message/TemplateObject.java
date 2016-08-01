package rook.ui.websocket.message;

import java.util.ArrayList;
import java.util.List;

public class TemplateObject {
	private List<TemplateField> fields;

	public List<TemplateField> getFields() {
		return fields;
	}

	public TemplateObject setFields(List<TemplateField> fields) {
		this.fields = fields;
		return this;
	}
	
	public TemplateField addField() {
		if(fields == null)
			fields = new ArrayList<>();
		TemplateField f = new TemplateField();
		fields.add(f);
		return f;
	}
	
}
