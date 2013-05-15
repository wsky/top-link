package com.taobao.top.link.endpoint;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;

public class MessageHandlerWrapper implements MessageHandler {
	public CountDownLatch latch;
	public Object sync = new Object();
	public AtomicInteger receive = new AtomicInteger();
	public HashMap<String, String> lastMessage;

	public boolean doError;
	public boolean doReply;
	public boolean print;

	@Override
	public void onMessage(HashMap<String, String> message) {
		lastMessage = message;
		receive.incrementAndGet();
		if (print)
			System.out.println("MessageHandlerWrapper-onMessage: " + message);
	}

	@Override
	public void onMessage(EndpointContext context) throws Exception {
		lastMessage = context.getMessage();
		receive.incrementAndGet();
		if (print)
			System.out.println("MessageHandlerWrapper-onMessage: " + context.getMessage());
		if (doError) {
			System.out.println("but doError=true");
			throw new Exception("process error");
		}
		if (doReply)
			context.reply(context.getMessage());
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

	public void clear() {
		lastMessage = null;
		doError = false;
		receive = new AtomicInteger();
		latch = null;
	}

	public void notifyHandler() {
		if (latch != null)
			latch.countDown();
		synchronized (this.sync) {
			this.sync.notify();
		}
	}

	public void assertHandler(int receive) {
		Assert.assertEquals(receive, this.receive.get());
	}
}
