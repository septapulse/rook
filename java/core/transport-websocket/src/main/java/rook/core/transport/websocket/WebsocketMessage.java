package rook.core.transport.websocket;

class WebsocketMessage {

	private WebsocketMessageType type;
	private String from;
	private String to;
	private String group;
	private String data;
	
	public WebsocketMessage(WebsocketMessageType type, String from, String to, String group, String data) {
		this.type = type;
		this.from = from;
		this.to = to;
		this.group = group;
		this.data = data;
	}
	public WebsocketMessageType getType() {
		return type;
	}
	public String getFrom() {
		return from;
	}
	public String getTo() {
		return to;
	}
	public String getGroup() {
		return group;
	}
	public String getData() {
		return data;
	}
	@Override
	public String toString() {
		return "WebsocketMessage [type=" + type + ", from=" + from + ", to=" + to + ", group=" + group + ", data="
				+ data + "]";
	}
	
}
