package com.taobao.top.link;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;

import com.taobao.top.link.handler.ChannelHandler;

public class ChannalHandlerWrapper implements ChannelHandler {
	public Object sync = new Object();

	public AtomicInteger receive = new AtomicInteger();
	public AtomicInteger error = new AtomicInteger();

	@Override
	public void onReceive(ByteBuffer dataBuffer, EndpointContext context) {
		receive.incrementAndGet();
		this.notifyHandler();
	}

	@Override
	public void onException(Throwable exception) {
		error.incrementAndGet();
		this.notifyHandler();
	}

	public void waitHandler() throws InterruptedException {
		this.waitHandler(0);
	}

	public void waitHandler(int timeout) throws InterruptedException {
		synchronized (this.sync) {
			if (timeout > 0)
				this.sync.wait(timeout);
			else
				this.sync.wait();
		}
	}

	public void notifyHandler() {
		synchronized (this.sync) {
			this.sync.notify();
		}
	}

	public void assertHandler(int receive, int error) {
		Assert.assertEquals(receive, this.receive.get());
		Assert.assertEquals(error, this.error.get());
	}
}
