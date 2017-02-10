package run.rook.api.transport.consumer;

public interface ProxyBroadcastMessageConsumer<T> extends BroadcastMessageConsumer<T> {
	BroadcastMessageConsumer<?> getBaseConsumer();
}
