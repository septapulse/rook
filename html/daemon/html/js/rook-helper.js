function template_create(template_id, new_id, parent) {
  var template = document.getElementById(template_id);
  var copy = template.cloneNode(true);
  copy.id=new_id;
  if(parent != null) {
	parent.append(copy);  
  }
  $(copy).show();
  return $(copy);
}