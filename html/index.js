function init() {
  window.onload = function() {
    addModule("Packages", "packages.html", "package.png");
    addModule("Processes", "processes.html", "process.png");
    //addModule("Sensors", "sensors.html", "sensors.png");
  }
}

function addModule(name, url, image) {
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