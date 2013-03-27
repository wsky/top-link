package com.taobao.top.link.endpoint;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;

public class MessageHandlerWrapper implements MessageHandler {
	public Object sync = new Object();
	public AtomicInteger receive = new AtomicInteger();

	@Override
	public void onMessage(EndpointContext context) throws Exception {
		receive.incrementAndGet();
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

	public void assertHandler(int receive) {
		Assert.assertEquals(receive, this.receive.get());
	}
}
