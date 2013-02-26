package com.taobao.top.link.remoting;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.taobao.top.link.ChannelException;
import com.taobao.top.link.ClientChannel;
import com.taobao.top.link.EndpointContext;
import com.taobao.top.link.handler.ChannelHandler;

// one handler per channel
public class RemotingClientChannelHandler extends ChannelHandler {
	private AtomicInteger integer = new AtomicInteger(0);
	private HashMap<String, RemotingCallback> callbacks = new HashMap<String, RemotingCallback>();

	// one-way
	// two-way
	// request-reply
	public void pending(ClientChannel channel,
			byte[] data, int offset, int length, RemotingCallback handler) throws ChannelException {
		int flag = this.integer.incrementAndGet();
		// TODO:buffer usage should be refactor
		ByteBuffer buffer = ByteBuffer.wrap(new byte[length + 4]);
		buffer.putInt(flag);
		buffer.put(data, offset, length);

		handler.flag = flag + "";
		this.callbacks.put(handler.flag, handler);// concurrent?

		channel.send(buffer.array(), buffer.arrayOffset(), buffer.capacity());
	}

	public void cancel(RemotingCallback handler) {
		this.callbacks.remove(handler.flag);
	}

	@Override
	public void onReceive(byte[] data, int offset, int length, EndpointContext context) {
		ByteBuffer buffer = ByteBuffer.wrap(data, offset, length);
		String flag = buffer.getInt() + "";// poor perf?
		RemotingCallback handler = this.callbacks.remove(flag);
		if (handler != null)
			handler.onReceive(buffer);
	}

	@Override
	public void onException(Throwable exception) {
		// all is fail!
		for (Entry<String, RemotingCallback> i : this.callbacks.entrySet()) {
			try {
				i.getValue().onException(exception);
			} catch (Exception e) {
			}
		}
		this.callbacks = new HashMap<String, RemotingCallback>();
	}
}
