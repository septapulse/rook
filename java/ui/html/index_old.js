function init() {
  if(location.hash.length<2) location='#default';
  rook_env_log(handle_log);
  rook_io_log(handle_io);
  rook_script_console(handle_script_console);
  _script_load();
}

function _bottomup_init() {
  sortable = Sortable.create(document.getElementById('bottomup_items'));
}

function _script_load() {
  rook_script_get(function(data) {
    var json = JSON.parse(data);
    document.getElementById('script').value = json.code;
  });
}

function _script_save() {
  rook_script_save(document.getElementById('script').value, null);
}

function _script_run() {
  document.getElementById('script_console').value="";
  _script_save();
  rook_script_start(null);
}

function _script_stop() {
  rook_script_stop(null);
}

function _start_environment() {
  document.getElementById('env_log').value="";
  document.getElementById("inputs").innerHTML="";
  document.getElementById("outputs").innerHTML="";
  rook_env_start(document.getElementById('env_config_name').value, dump_env_result);
}

function _stop_environment() {
  rook_env_stop(dump_env_result);
  rook_script_stop(null);
}

function dump_cfg_result(result) {
  document.getElementById("cfg_result").innerHTML = result;
}

function dump_env_result(result) {
  document.getElementById("env_result").innerHTML = result;
}

function handle_log(text) {
  var e = document.getElementById('env_log');
  e.value += text + '\n';
  e.scrollTop = e.scrollHeight;
}

function handle_script_console(text) {
  var e = document.getElementById('script_console');
  e.value += text + '\n';
  e.scrollTop = e.scrollHeight;
}

function handle_io(json) {
  if(json.v != null) {
    var t = json.t;
    json.v.forEach(function(v) {
      var eId = t + "_" + v.id;
      var e = document.getElementById(eId);
      if(e == null) {
        var div = null;
        if(t == "inputs") {
          div = document.getElementById("inputs");
        } else if(t == "outputs") {
          div = document.getElementById("outputs");
        }
        div.innerHTML += "<table class='kvp'><tr><td class='key'><label class='key'>" + v.id + "</label></td><td class='key'><label id='" + eId + "' class='value'></label></td></tr></table>";
        e = document.getElementById(eId);
      }
      if(e != null) {
        e.innerHTML = v.v
      }
    });
  }
}
