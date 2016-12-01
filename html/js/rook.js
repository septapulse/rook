function rook_services_get_packages(response_handler) {
  rook_send_listen_json_disconnect("services", '{ "t": "get_packages" }', response_handler);
}

function rook_services_get_package_info(pkg, response_handler) {
  rook_send_listen_json_disconnect("services", '{ "t": "get_package_info", "pkg": "' + pkg + '" }', response_handler);
}

function rook_cfg_get_service_configs(pkg, sid, response_handler) {
  rook_send_listen_json_disconnect("cfg", '{ "t": "get_configs", "pkg": "' + pkg + '", "sid": "' + sid + '" }', response_handler);
}

function rook_runtime_get_running(response_handler) {
  rook_send_listen_json_disconnect("runtime", '{ "t": "get_running" }', response_handler);
}

function rook_runtime_start_service(pkg, sid, cfg, response_handler) {
  var obj = { };
  obj.t = "start_service";
  obj.pkg = pkg;
  obj.sid = sid;
  obj.cfg = cfg;
  var json = JSON.stringify(obj);
  rook_send_listen_json_disconnect("runtime", json, response_handler);
}

function rook_runtime_start_bridge(pkg, sid, cfg, response_handler) {
  var obj = { };
  obj.t = "start_bridge";
  obj.pkg = pkg;
  obj.sid = sid;
  obj.cfg = cfg;
  var json = JSON.stringify(obj);
  rook_send_listen_json_disconnect("runtime", json, response_handler);
}

function rook_runtime_start_router(pkg, sid, cfg, response_handler) {
  var obj = { };
  obj.t = "start_router";
  obj.pkg = pkg;
  obj.sid = sid;
  obj.cfg = cfg;
  var json = JSON.stringify(obj);
  rook_send_listen_json_disconnect("runtime", json, response_handler);
}

function rook_runtime_stop(uid, response_handler) {
  rook_send_listen_json_disconnect("runtime", '{ "t": "stop", "uid": "'+uid+'" }', response_handler);
}

function rook_runtime_open_log_stream(uid, response_handler) {
  return rook_send_listen_json_forever("runtime", '{ "t": "open_log_stream", "uid": "'+uid+'" }', response_handler);
}

function rook_io_stream(response_handler) {
  return rook_send_listen_json_forever("io", '{ "t": "stream" }', response_handler);
}

function rook_send_listen_json_disconnect(protocol, text, response_handler) {
  var ws = new WebSocket("ws://" + window.location.host + "/ws", protocol);
  ws.onmessage = function (evt)
  {
    if(response_handler != null) {
      var json = JSON.parse(evt.data);
      response_handler(json);
    }
    ws.close();
  };
  ws.onopen = function()
  {
    ws.send(text);
  };
}

function rook_send_listen_disconnect(protocol, text, response_handler) {
  var ws = new WebSocket("ws://" + window.location.host + "/ws", protocol);
  ws.onmessage = function (evt)
  {
    if(response_handler != null) {
      response_handler(evt.data);
    }
    ws.close();
  };
  ws.onopen = function()
  {
    ws.send(text);
  };
}

function rook_send_listen_json_forever(protocol, text, response_handler) {
  var ws = new WebSocket("ws://" + window.location.host + "/ws", protocol);
  ws.onmessage = function (evt)
  {
    var json = JSON.parse(evt.data);
    response_handler(json);
  };
  ws.onopen = function()
  {
    ws.send(text);
  };
  return ws;
}
