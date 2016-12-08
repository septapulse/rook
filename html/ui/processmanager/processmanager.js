function init() {
  window.onload = function() {
    $("#button_start").attr("onclick", "open_start_dialog()");
  	rook_daemon_process_list(handle_processes);
  }
}

function handle_processes(json) {
  if(json.processes != null) {
    json.processes.forEach(function(process) {
      add_process(process.id, process.packageName, process.serviceName);
    });
  }
}

function add_process(id, packageName, serviceName) {
  var node = template_create("template_process", "process_"+id, $("body"));
  node.find('[name="service"]').html(serviceName);
  node.find('[name="package"]').html(packageName);
  // TODO log
}

function open_start_dialog() {
  $("#dialog_content").html("");
  rook_daemon_package_list(handle_dialog_packages);
  document.getElementById("dialog").showModal();
}

function handle_dialog_packages(json) {
  if(json.packages != null) {
    json.packages.forEach(function(pkg) {
      add_dialog_package(pkg.id, pkg.name);
    });
  }
}

function add_dialog_package(id, name) {
  var node = template_create("template_package", "package_"+id, $("#dialog_content"));
  node.find('[name="name"]').html(name);
  node.find('[name="button"]').attr("onclick", "handle_dialog_package_click(\"" + id + "\")");
}

function handle_dialog_package_click(packageId) {
  $("#dialog_content").html("");
  rook_daemon_package_get(packageId, handle_dialog_services);
}

function handle_dialog_services(json) {
  for (var key in json.pkg.services) {
    if (json.pkg.services.hasOwnProperty(key)) {
      service = json.pkg.services[key];
      add_dialog_service(json.pkg.id, service.id, service.name);
    }
  }
}

function add_dialog_service(packageId, serviceId, serviceName) {
  var node = template_create("template_package", "service_"+serviceId, $("#dialog_content"));
  node.find('[name="name"]').html(serviceName);
  node.find('[name="button"]').attr("onclick", "handle_dialog_service_click(\"" + packageId + "\", \"" + serviceId + "\")");
}

function handle_dialog_service_click(packageId, serviceId) {
  $("#dialog_content").html("");
}