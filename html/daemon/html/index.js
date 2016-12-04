function init() {
  window.onload = function() {
  	rook_daemon_services_get_uis(handle_uis);
  }
}

function handle_uis(json) {
  if(json.uis != null) {
    json.uis.forEach(function(ui) {
      handle_ui(ui);
    });
  }
}

function handle_ui(ui) {
  var path = "/ui/"+ui.id+"/";
  add_module(ui.name, path, path+ui.image);
}


function add_module(name, url, image) {
  var node = template_create("template_module", null, $("body"));
  node.find('[name="name"]').html(name);
  node.find('[name="image"]').attr("src", image);
  node.find('[name="button"]').attr("onclick", " window.open(\"" + url + "\")");
}

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