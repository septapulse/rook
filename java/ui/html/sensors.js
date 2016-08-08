function init() {
  rook_io_stream(handle_io);
}

function handle_io(json) {
  if(json.v != null) {
    var t = json.t;
    json.v.forEach(function(v) {
      var table = $("#"+t);
      var rowId = t + "_" + v.id;
      if($("#"+rowId).length == 0) {
        template_create("template_row", rowId, table)
            .find('[name="key"]').html(v.id);
      }
      $("#"+rowId).find('[name="value"]').html(v.v);
    });
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