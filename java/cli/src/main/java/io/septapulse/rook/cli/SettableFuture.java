package io.septapulse.rook.cli;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class SettableFuture<V> implements Future<V> {
	
	private volatile V result;
	private volatile Throwable failReason;
	private volatile boolean done;
	private volatile boolean cancelled;
	private volatile boolean interrupted;
	
	public void set(V value) {
		synchronized (this) {
			if(!done) {
				result = value;
				done = true;
				this.notifyAll();
			}
		}
	}
	
	public void fail(Throwable reason) {
		synchronized (this) {
			if(!done) {
				failReason = reason;
				done = true;
				this.notifyAll();
			}
		}
	}
	
	public void interrupt() {
		synchronized (this) {
			if(!done) {
				interrupted = true;
				done = true;
				this.notifyAll();
			}
		}
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		synchronized (this) {
			if(!done) {
				cancelled = true;
				done = true;
				this.notifyAll();
			}
		}
		return cancelled;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		synchronized (this) {
			while(!done) {
				wait();
			}
		}
		if(interrupted) {
			throw new InterruptedException();
		} else if(failReason != null) {
			throw new ExecutionException(failReason);
		} else {
			return result;
		}
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		if(timeout < 0) {
			throw new IllegalArgumentException("Timeout is negative");
		}
		final long millis = unit.toMillis(timeout);
		synchronized (this) {
			if(!done) {
				wait(millis);
			}
		}
		
		if(interrupted) {
			throw new InterruptedException();
		} else if(failReason != null) {
			throw new ExecutionException(failReason);
		} else if(done) {
			return result;
		} else {
			throw new TimeoutException();
		}
	}

}
