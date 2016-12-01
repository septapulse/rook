var log_sessions = {};

function init() {
  rook_services_get_packages(handle_packages);
}

function handle_packages(json) {
  if(json.packages != null) {
    json.packages.forEach(function(package) {
      handle_package(package);
    });
  }
  // run only after packages are received
  rook_runtime_get_running(handle_running_instances);
}

function handle_package(package) {
  var node = template_create("template_package", "pkg_"+package, $("#package_list"));
  node.find('[name="title"]').html(package);
  node.find('[name="add_instance"]').attr("onclick", "open_package_dialog(\"" + package + "\")");
}

function handle_running_instances(json) {
  if(json.running != null) {
    json.running.forEach(function(instance) {
      handle_running_instance(instance);
    });
  }
}

function handle_running_instance(inst) {
  // make sure package exists
  if(!($("#pkg_"+inst.pkg).length > 0))
    return;
  // create
  var node = template_create("template_instance", "inst_"+inst.uid, $("#pkg_"+inst.pkg).find('[name=running]'));
  node.find('[name="name"]').html(inst.name);
  node.find('[name="stop"]').attr("onclick", "stop_service("+inst.uid+")");
  node.find('[name="log_toggle"]').attr("onclick", "toggle_log_stream("+inst.uid+")");
}

function stop_service(uid) {
  rook_runtime_stop(uid, handle_stopped_service);
}

function toggle_log_stream(uid) {
  if($("#log_"+uid).length == 0) {
    open_log_stream(uid);
  } else {
    close_log_stream(uid);
  }
}

function close_log_stream(uid) {
  var ws = log_sessions[uid];
  if(ws != null) {
    ws.close();
    delete log_sessions[uid];
  } 
  var log = $("#log_"+uid);
  if(log.length > 0) {
    log.remove();
  }
  $("#inst_"+uid).find('[name=log_toggle]').removeClass("selected");
}

function open_log_stream(uid) {
  template_create("template_log", "log_"+uid, $("#inst_"+uid));
  log_sessions[uid] = rook_runtime_open_log_stream(uid, handle_log, log_sessions);
  $("#inst_"+uid).find('[name=log_toggle]').addClass("selected");;
}

function handle_log(json) {
  var textarea = $("#inst_"+json.uid).find('[name=log]')[0];
  if(textarea != null) {
    textarea.value += json.m;
    textarea.value += "\r\n";
    textarea.scrollTop = textarea.scrollHeight;
  }
}

function handle_stopped_service(json) {
  if(json.success && json.instance != null) {
    $("#inst_"+json.instance.uid).remove();
  }
}

function open_package_dialog(pkg) {
  rook_services_get_package_info(pkg, handle_package_dialog);
}

function handle_package_dialog(json) {
  var dialog = $("#dialog_div").empty();
  
  template_create("template_dialog_title", null, dialog)
    .html("Service Type");
  
  if(json.packageInfo != null && json.packageInfo.services != null) {
    json.packageInfo.services.forEach(function(service) {
      template_create("template_dialog_select", null, dialog)
          .html(service.name)
          .attr("onclick", "open_select_config_dialog(\""+service.pkg+"\",\""+service.id+"\")");
    });
  }
  template_create("template_dialog_select", null, dialog)
      .html("Cancel")
      .attr("onclick", "document.getElementById(\"dialog\").close()");
  document.getElementById("dialog").showModal();
}

function open_select_config_dialog(pkg, sid) {
  document.getElementById("dialog").close();
  rook_cfg_get_service_configs(pkg, sid, handle_select_config_dialog);
}

function handle_select_config_dialog(json) {
  var dialog = $("#dialog_div").empty();
  
  template_create("template_dialog_title", null, dialog)
    .html("Configuration");
  
  if(json.cfgs != null) {
    json.cfgs.forEach(function(config) {
      template_create("template_dialog_select", null, dialog)
          .html(config.configName)
          .attr("onclick", "start_service(\"" + config.pkg + "\",\"" + config.sid + "\",\"" + config.configName + "\")");
    });
  }
  template_create("template_dialog_select", null, dialog)
      .html("Cancel")
      .attr("onclick", "document.getElementById(\"dialog\").close()");
  document.getElementById("dialog").showModal();
}

function start_service(pkg, sid, cfg) {
  document.getElementById("dialog").close();
  rook_runtime_start_service(pkg, sid, cfg, handle_service_start);
}

function handle_service_start(json) {
  if(json.instance != null) {
  	handle_running_instance(json.instance);
  }
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