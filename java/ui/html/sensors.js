function init() {
  rook_io_stream(handle_io);
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