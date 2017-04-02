function init() {
  listen();
}

function handle_io(json) {
  if(json.v != null) {
    var t = json.t;
    json.v.forEach(function(v) {
      handle_value(t, v.id, v.v);
    });
  }
}

function handle_value(t, id, v) {
  var table = $("#"+t);
  var rowId = t + "_" + id;
  if($("#"+rowId).length == 0) {
    template_create("template_row", rowId, table)
        .find('[name="key"]').html(id);
  }
  $("#"+rowId).find('[name="value"]').html(v);
}

function listen() {
  var ws = new WebSocket("ws://" + window.location.host + "/ws", "router");
  ws.onmessage = function (evt)
  {
    var json = JSON.parse(evt.data);
    if(json.type == "BROADCAST") {
      var chars = atob(json.data);
      var bytes = new Array(chars.length);
      for (var i = 0; i < chars.length; i++) {
        bytes[i] = chars.charCodeAt(i);
      }
      for (var i = 0; i < bytes.length; ) {
        var id = decodeID(bigInt(toHex(bytes, i, 8), 16));
        i+=8;
        var type = bytes[i++];
        var length = (bytes[i++]) | (bytes[i++] << 8) | (bytes[i++] << 16) | (bytes[i++] << 24);
        var valueBytes = new Array(length);
        for(var j = 0; j < length; j++) {
          valueBytes[j] = bytes[i++];
        }
        
        var value = 0;
        if(type == 1) {
          // int
          for(var j = 0; j < valueBytes.length; j++) {
            value |= (valueBytes[j] << (j*8));
          }
        } else if(type == 2) {
          // float
          // FIXME support
          value = "NaN";
        } else if(type == 3) {
          // boolean
          value = valueBytes[0] > 0 ? true : false;
        } else if(type == 4) {
          // UTF-8 string
          value = "";
          for (var j = 0; j < valueBytes.length; j++) {
   			value += String.fromCharCode(parseInt(valueBytes[i], 2));
          }
        } else {
          // opaque
          value = btoa(valueBytes);
        }
        
        handle_value(json.group == "IO.OUTPUTS" ? "outputs" : "inputs", id, value);
      }
    }
  };
  ws.onopen = function()
  {
    ws.send('{ "type": "REGISTER", "from": "UI_SENSORS" }');
    ws.send('{ "type": "JOIN", "from": "UI_SENSORS", "group": "IO.INPUT" }');
    ws.send('{ "type": "JOIN", "from": "UI_SENSORS", "group": "IO.OUTPUT" }');
  };
}

function toHex(bytes, off, len) {
  var hex = "";
  for(var i = 0; i < len; i++) {
    hex = ('0' + (bytes[off+i] & 0xFF).toString(16)).slice(-2) + hex;
  }
  return hex;
}

function decodeID(i) {
  var ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_.";
  var result = "";
  while (i > 0) {
    var rem = i.mod(ALPHABET.length);
    result = ALPHABET.charAt(rem) + result;
    i = i.divide(ALPHABET.length);
  }
  return result;
}
