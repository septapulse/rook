package rook.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import rook.api.Service;
import rook.api.RID;
import rook.api.transport.Deserializer;
import rook.api.transport.Transport;
import rook.api.transport.event.BroadcastMessage;
import rook.api.transport.event.UnicastMessage;

public class QueueService<T> implements Service {

	private final BlockingQueue<BroadcastMessage<T>> bcastQueue = new LinkedBlockingQueue<>();
	private final BlockingQueue<UnicastMessage<T>> ucastQueue = new LinkedBlockingQueue<>();
	private final BlockingQueue<UnicastMessage<T>> ucastTapQueue = new LinkedBlockingQueue<>();
	private final List<Pair<RID, Consumer<BroadcastMessage<T>>>> bcastConsumers = new ArrayList<>();
	
	private final Deserializer<T> deserializer;
	private Transport transport;
	
	public QueueService(Deserializer<T> deserializer) {
		this.deserializer = deserializer;
	}
	
	public void reset() {
		bcastQueue.clear();
		ucastQueue.clear();
		ucastTapQueue.clear();
		for(Pair<RID, Consumer<BroadcastMessage<T>>> p : bcastConsumers) {
			transport.bcast().removeMessageConsumer(p.first, p.second);
		}
	}
	
	public BlockingQueue<BroadcastMessage<T>> getBcastQueue() {
		return bcastQueue;
	}
	
	public BlockingQueue<UnicastMessage<T>> getUcastQueue() {
		return ucastQueue;
	}
	
	public BlockingQueue<UnicastMessage<T>> getUcastTapQueue() {
		return ucastTapQueue;
	}
	
	@Override
	public void setTransport(Transport transport) {
		this.transport = transport;
	}

	public Transport getTransport() {
		return transport;
	}
	
	@Override
	public void init() {
		transport.ucast().addMessageConsumer(m->ucastQueue.add(copy(m)), deserializer);
		transport.ucast().incognito_addMessageConsumer(m->ucastTapQueue.add(copy(m)), deserializer);
	}
	
	private UnicastMessage<T> copy(UnicastMessage<T> m) {
		UnicastMessage<T> copy = new UnicastMessage<>();
		copy.getFrom().setValue(m.getFrom().toValue());
		copy.getTo().setValue(m.getTo().toValue());
		copy.setPayload(m.getPayload());
		return copy;
	}
	
	public void bcastJoin(RID group) {
		Consumer<BroadcastMessage<T>> c = m->bcastQueue.add(copy(m));
		transport.bcast().addMessageConsumer(group, c, deserializer);
		bcastConsumers.add(new Pair<RID, Consumer<BroadcastMessage<T>>>(group, c));
	}
	
	public void bcastLeave(RID group) {
		for(Pair<RID, Consumer<BroadcastMessage<T>>> p : bcastConsumers) {
			if(p.first.equals(group)) {
				transport.bcast().removeMessageConsumer(group, p.second);
			}
		}
	}
	
	private BroadcastMessage<T> copy(BroadcastMessage<T> m) {
		BroadcastMessage<T> copy = new BroadcastMessage<>();
		copy.getFrom().setValue(m.getFrom().toValue());
		copy.getGroup().setValue(m.getGroup().toValue());
		copy.setPayload(m.getPayload());
		return copy;
	}

	@Override
	public void shutdown() {
		
	}
	
	private static class Pair<F,S> {
		public final F first;
		public final S second;
		public Pair(F first, S second) {
			this.first = first;
			this.second = second;
		}
	}
}
