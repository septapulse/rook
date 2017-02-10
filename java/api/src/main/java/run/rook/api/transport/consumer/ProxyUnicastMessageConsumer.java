package run.rook.api.transport.consumer;

public interface ProxyUnicastMessageConsumer<T> extends UnicastMessageConsumer<T> {
	UnicastMessageConsumer<?> getBaseConsumer();
}
