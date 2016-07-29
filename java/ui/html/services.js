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
  var ul = document.getElementById("service_list");
  ul.innerHTML += "<li><h2>" + package + "</h2><div id='"+package+"_running'></div><div class='contentParent'><div class='contentLeft'><button type='button' onclick='open_package_dialog(\"" + package + "\")'>+</button></div></div>";
}

function handle_running_instances(json) {
  if(json.running != null) {
    json.running.forEach(function(instance) {
      handle_running_instance(instance);
    });
  }
}

function handle_running_instance(inst) {
  var div = document.getElementById(inst.pkg+"_running");
  if(div != null) {
  	div.innerHTML += "<div class='contentParent' id='uid_"
  	    + inst.uid+"'><div class='contentLeft'><button type='button' onclick='stop_service("
  	    + inst.uid + ")'>Stop</button><button type='button' id='btn_log_"
  	    + inst.uid +"' onclick='toggle_log_stream("
  	    + inst.uid + ")'>Log</button></div><div class='contentRight'><h4>"
  	    + inst.name + "</h4></div></div>";
  }
}

function stop_service(uid) {
  rook_runtime_stop(uid, handle_stopped_service);
}

function toggle_log_stream(uid) {
  if(log_sessions[uid] == null) {
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
  var div = document.getElementById("div_log_"+uid);
  if(div != null) {
    div.parentNode.removeChild(div);
  }
  document.getElementById("btn_log_"+uid).style.background='#585858';
}

function open_log_stream(uid) {
  var parentDiv = document.getElementById("uid_"+uid);
  parentDiv.innerHTML += "<div id='div_log_"+uid+"'><log><textarea wrap='off' readonly id='log_"+uid+"'></textarea></log></div>";
  log_sessions[uid] = rook_runtime_open_log_stream(uid, handle_log, log_sessions);
  document.getElementById("btn_log_"+uid).style.background='#ff5800';
}

function handle_log(json) {
  //console.log("handle_log: " + json.uid + " " + json.m);
  var textarea = document.getElementById("log_"+json.uid);
  if(textarea != null) {
    textarea.value += json.m;
    textarea.value += "\r\n";
    textarea.scrollTop = textarea.scrollHeight;
  }
}

function handle_stopped_service(json) {
  if(json.success && json.instance != null) {
    var parent = document.getElementById(json.instance.pkg+"_running");
    var div = document.getElementById("uid_"+json.instance.uid);
    parent.removeChild(div);
  }
}

function open_package_dialog(pkg) {
  rook_services_get_package_info(pkg, handle_package_dialog);
}

function handle_package_dialog(json) {
  var div = document.getElementById("dialog_div");
  div.innerHTML = "<h2 class='orangeContainer'>Service Type</h2>";
  if(json.packageInfo != null && json.packageInfo.services != null) {
    json.packageInfo.services.forEach(function(service) {
      div.innerHTML += "<button type='button' onclick='open_select_config_dialog(\""+service.pkg+"\",\""+service.id+"\")'>"+service.name+"</button>";
    });
  }
  div.innerHTML += "<button type='button' onclick='document.getElementById(\"dialog\").close()'>Cancel</button>";
  document.getElementById("dialog").showModal();
}

function open_select_config_dialog(pkg, sid) {
  document.getElementById("dialog").close();
  rook_cfg_get_service_configs(pkg, sid, handle_select_config_dialog);
}

function handle_select_config_dialog(json) {
  var div = document.getElementById("dialog_div");
  div.innerHTML = "<h2 class='orangeContainer'>Configuration</h2>";  
  if(json.cfgs != null) {
    json.cfgs.forEach(function(config) {
      div.innerHTML += "<button type='button' onclick='start_service(\""
                       + config.pkg + "\",\"" + config.sid + "\",\"" + config.configName
                       + "\")'>" + config.configName + "</button>";
    });
    div.innerHTML += "<button type='button' onclick='document.getElementById(\"dialog\").close()'>Cancel</button>";
    document.getElementById("dialog").showModal();
  }
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