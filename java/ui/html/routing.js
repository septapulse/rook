var log_sessions = {};

function init() {
  rook_runtime_get_running(handle_running_instances);
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
  	    + inst.uid+"'><div class='contentLeft'><button type='button' onclick='stop_bridge("
  	    + inst.uid + ")'>Stop</button><button type='button' id='btn_log_"
  	    + inst.uid +"' onclick='toggle_log_stream("
  	    + inst.uid + ")'>Log</button></div><div class='contentRight'><h4>"
  	    + inst.name + "</h4></div></div>";
  }
}

function stop_bridge(uid) {
  rook_runtime_stop(uid, handle_stopped_bridge);
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

function handle_stopped_bridge(json) {
  if(json.success && json.instance != null) {
    var parent = document.getElementById(json.instance.pkg+"_running");
    var div = document.getElementById("uid_"+json.instance.uid);
    parent.removeChild(div);
  }
}

function open_dialog(title, router_function_name, bridge_function_name) {
  var div = document.getElementById("dialog_div");
  div.innerHTML = "<h2 class='orangeContainer'>"+title+"</h2>";
  if(router_function_name != null) {
  	div.innerHTML += "<button type='button' onclick='"+router_function_name+"()'>Router</button>";
  }
  if(bridge_function_name != null) {
  	div.innerHTML += "<button type='button' onclick='"+bridge_function_name+"()'>Bridge</button>";
  }
  div.innerHTML += "<button type='button' onclick='document.getElementById(\"dialog\").close()'>Cancel</button></div>";
  document.getElementById("dialog").showModal();
}

function open_mqtt_bridge_dialog() {
  var div = document.getElementById("dialog_div");
  div.innerHTML = "<h2 class='orangeContainer'>MQTT Configuration</h2>";
  div.innerHTML += "<div><h4>Host</h4><input id='mqtt_host' type='text' value='localhost'></input></div>";
  div.innerHTML += "<div><h4>Port</h4><input id='mqtt_port' type='text' value='1883'></input></div>";
  div.innerHTML += "<div><button type='button' onclick='start_mqtt_bridge()'>Start</button>";
  div.innerHTML += "<button type='button' onclick='document.getElementById(\"dialog\").close()'>Cancel</button></div>";
}

function start_mqtt_bridge() {
  var host = document.getElementById("mqtt_host").value;
  var port = document.getElementById("mqtt_host").value;
  var cfg = '{ "host": "' +host+ '", "port": "' +port+ '" }';
  start_bridge("mqtt", "rook.api.transport.mqtt.MqttTransport", cfg);
  document.getElementById("dialog").close();
}

function open_tcp_bridge_dialog() {
  var div = document.getElementById("dialog_div");
  div.innerHTML = "<h2 class='orangeContainer'>TCP Bridge Config</h2>";
  div.innerHTML += "<div><h4>Host</h4><input id='tcp_host' type='text' value='localhost'></input></div>";
  div.innerHTML += "<div><h4>Port</h4><input id='tcp_port' type='text' value='9001'></input></div>";
  div.innerHTML += "<div><button type='button' onclick='start_tcp_bridge()'>Start</button>";
  div.innerHTML += "<button type='button' onclick='document.getElementById(\"dialog\").close()'>Cancel</button></div>";
}

function start_tcp_bridge() {
  var host = document.getElementById("tcp_host").value;
  var port = document.getElementById("tcp_port").value;
  var cfg = '{ "host": "' +host+ '", "port": "' +port+ '" }';
  start_bridge("tcp", "rook.api.transport.tcp.TcpTransport", cfg);
  document.getElementById("dialog").close();
}

function open_tcp_router_dialog() {
  var div = document.getElementById("dialog_div");
  div.innerHTML = "<h2 class='orangeContainer'>TCP Router Config</h2>";
  div.innerHTML += "<div><h4>Port</h4><input id='tcp_port' type='text' value='9001'></input></div>";
  div.innerHTML += "<div><button type='button' onclick='start_tcp_router()'>Start</button>";
  div.innerHTML += "<button type='button' onclick='document.getElementById(\"dialog\").close()'>Cancel</button></div>";
}

function start_tcp_router() {
  var port = document.getElementById("tcp_port").value;
  var cfg = '{ "port": "' +port+ '" }';
  start_router("tcp", "rook.api.transport.tcp.TcpRouter", cfg);
  document.getElementById("dialog").close();
}

function open_aeron_bridge_dialog() {
  var div = document.getElementById("dialog_div");
  div.innerHTML = "<h2 class='orangeContainer'>Aeron Bridge Config</h2>";
  div.innerHTML += "<div><h4>Directory</h4><input id='aeron_dir' type='text' value='/dev/shm/rook'></input></div>";
  div.innerHTML += "<div><h4>Channel</h4><input id='aeron_channel' type='text' value='aeron:ipc'></input></div>";
  div.innerHTML += "<div><h4>StreamID</h4><input id='aeron_stream' type='text' value='1'></input></div>";
  div.innerHTML += "<div><button type='button' onclick='start_aeron_bridge()'>Start</button>";
  div.innerHTML += "<button type='button' onclick='document.getElementById(\"dialog\").close()'>Cancel</button></div>";
}

function start_aeron_bridge() {
  var dir = document.getElementById("aeron_dir").value;
  var channel = document.getElementById("aeron_channel").value;
  var stream = document.getElementById("aeron_stream").value;
  var cfg = '{ "aeronDirectoryName": "' +dir+ '", "channel": "' +channel+ '", "streamId": "' +stream+ '" }';
  start_bridge("aeron", "rook.api.transport.aeron.AeronTransport", cfg);
  document.getElementById("dialog").close();
}

function open_aeron_router_dialog() {
  var div = document.getElementById("dialog_div");
  div.innerHTML = "<h2 class='orangeContainer'>Aeron Router Config</h2>";
  div.innerHTML += "<div><h4>Directory</h4><input id='aeron_dir' type='text' value='/dev/shm/rook'></input></div>";
  div.innerHTML += "<div><button type='button' onclick='start_aeron_router()'>Start</button>";
  div.innerHTML += "<button type='button' onclick='document.getElementById(\"dialog\").close()'>Cancel</button></div>";
}

function start_aeron_router() {
  var dir = document.getElementById("aeron_dir").value;
  var cfg = '{ "directoryName": "' +dir+ '" }';
  start_router("aeron", "rook.api.transport.aeron.AeronRouter", cfg);
  document.getElementById("dialog").close();
}


function start_bridge(pkg, sid, cfg) {
  rook_runtime_start_bridge(pkg, sid, cfg, handle_process_start);
}

function start_router(pkg, sid, cfg) {
  rook_runtime_start_router(pkg, sid, cfg, handle_process_start);
}

function handle_process_start(json) {
  if(json.instance != null) {
  	handle_running_instance(json.instance);
  }
}