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
      if(pkg.services != null) {
        for (var key in pkg.services) {
          if (pkg.services.hasOwnProperty(key)) {
            service = pkg.services[key];
            add_dialog_service(pkg.id, pkg.name, service.id, service.name);
          }
        }
      }
    });
  }
}

function add_dialog_service(packageId, packageName, serviceId, serviceName) {
  var node = template_create("template_service", null, $("#dialog_content"));
  node.find('[name="packageName"]').html(packageName);
  node.find('[name="serviceName"]').html(serviceName);
  node.find('[name="button"]').attr("onclick", "handle_dialog_service_click(\"" + packageId + "\", \"" + serviceId + "\")");
}

function handle_dialog_service_click(packageId) {
  $("#dialog_content").html("");
  // TODO
}
