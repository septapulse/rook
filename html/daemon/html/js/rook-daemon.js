function rook_daemon_ui_list(response_handler) {
  rook_daemon_send_listen_json_disconnect("UI", '{ "type": "LIST" }', response_handler);
}

function rook_daemon_process_list(response_handler) {
  rook_daemon_send_listen_json_disconnect("PROCESS", '{ "type": "LIST" }', response_handler);
}

function rook_daemon_package_list(response_handler) {
  rook_daemon_send_listen_json_disconnect("PACKAGE", '{ "type": "LIST" }', response_handler);
}

function rook_daemon_package_get(id, response_handler) {
  rook_daemon_send_listen_json_disconnect("PACKAGE", '{ "type": "GET", "id": "' + id + '" }', response_handler);
}

function rook_daemon_send_listen_json_disconnect(protocol, text, response_handler) {
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
