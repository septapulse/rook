function rook_daemon_services_get_uis(response_handler) {
  rook_daemon_send_listen_json_disconnect("UI", '{ "type": "LIST" }', response_handler);
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
