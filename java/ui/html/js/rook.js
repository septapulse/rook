function rook_cfg_get_configs(response_handler) {
  rook_send_listen_disconnect("cfg", "{ \"t\": \"get_configs\" }", response_handler);
}

function rook_cfg_get_config(config_name, response_handler) {
  rook_send_listen_disconnect("cfg", "{ \"t\": \"get_config\", \"name\": \"" + config_name + "\" }", response_handler);
}

function rook_cfg_set_config(config_name, config, response_handler) {
  rook_send_listen_disconnect("cfg", "{ \"t\": \"set_config\", \"name\": \"" + config_name + "\", \"cfg\": " + config + "\" }", response_handler);
}

function rook_cfg_get_services(response_handler) {
  rook_send_listen_disconnect("cfg", "{ \"t\": \"get_services\" }", response_handler);
}

function rook_cfg_get_template(library, service, response_handler) {
  rook_send_listen_disconnect("cfg", "{ \"t\": \"get_template\", \"library\": \"" + library + "\", \"name\": " + service + "\" }", response_handler);
}

function rook_env_log(log_handler) {
  rook_send_listen_json_forever("env", "{ \"t\": \"log\" }", function (json) { log_handler(json.m); } );
}

function rook_env_start(config, response_handler) {
  rook_send_listen_disconnect("env", "{ \"t\": \"start\", \"cfg\": \"" + config + "\" }", response_handler);
}

function rook_env_stop(response_handler) {
  rook_send_listen_disconnect("env", "{ \"t\": \"stop\" }", response_handler);
}

function rook_io_log(io_handler) {
  rook_send_listen_json_forever("io", "{ \"t\": \"stream\" }", io_handler);
}

function rook_script_save(code, response_handler) {
  rook_send_listen_disconnect("script", "{ \"t\": \"save\", \"code\": " + JSON.stringify(code) + " }", response_handler);
}

function rook_script_get(response_handler) {
  rook_send_listen_disconnect("script", "{ \"t\": \"get\" }", response_handler);
}

function rook_script_start(response_handler) {
  rook_send_listen_disconnect("script", "{ \"t\": \"start\" }", response_handler);
}

function rook_script_stop(response_handler) {
  rook_send_listen_disconnect("script", "{ \"t\": \"stop\" }", response_handler);
}

function rook_script_console(console_handler) {
  rook_send_listen_json_forever("script", "{ \"t\": \"console\" }", function (json) { console_handler(json.m); });
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
}
