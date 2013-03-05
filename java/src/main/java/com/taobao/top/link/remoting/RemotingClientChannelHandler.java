package com.taobao.top.link.remoting;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.taobao.top.link.BufferManager;
import com.taobao.top.link.ChannelException;
import com.taobao.top.link.ClientChannel;
import com.taobao.top.link.EndpointContext;
import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.handler.ChannelHandler;

public class RemotingClientChannelHandler extends ChannelHandler {
	private Logger logger;
	private AtomicInteger flag;
	private HashMap<String, RemotingCallback> callbacks = new HashMap<String, RemotingCallback>();

	public RemotingClientChannelHandler(LoggerFactory loggerFactory, AtomicInteger flag) {
		this.logger = loggerFactory.create(this);
		this.flag = flag;
	}

	public ByteBuffer pending(ClientChannel channel, RemotingCallback handler) throws ChannelException {
		int flag = this.flag.incrementAndGet();
		ByteBuffer buffer = BufferManager.getBuffer();
		buffer.putInt(flag);

		handler.flag = Integer.toString(flag);
		this.callbacks.put(handler.flag, handler);// concurrent?
		this.logger.debug("sending request of rpc-call#%s", flag);

		return buffer;
	}

	public void cancel(RemotingCallback handler) {
		this.callbacks.remove(handler.flag);
	}

	@Override
	public void onReceive(ByteBuffer dataBuffer, EndpointContext context) {
		String flag = dataBuffer.getInt() + "";// poor perf?
		RemotingCallback handler = this.callbacks.remove(flag);
		if (handler != null) {
			this.logger.debug("receive reply of rpc-call#%s", flag);
			handler.onReceive(dataBuffer);
		}
	}

	@Override
	public void onException(Throwable exception) {
		// all is fail!
		for (Entry<String, RemotingCallback> i : this.callbacks.entrySet()) {
			try {
				i.getValue().onException(exception);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.callbacks = new HashMap<String, RemotingCallback>();
	}
}
