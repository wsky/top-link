package com.taobao.top.link.remoting;

import java.nio.ByteBuffer;

import com.taobao.top.link.ChannelException;
import com.taobao.top.link.ClientChannel;

public class DynamicProxy {
	private ClientChannel channel;
	private RemotingClientChannelHandler channelHandler;

	protected DynamicProxy(ClientChannel channel, RemotingClientChannelHandler handler) {
		this.channel = channel;
		this.channelHandler = handler;
	}

	public ByteBuffer call(byte[] data, int offset, int length, int timeoutMillisecond) throws ChannelException {
		SynchronizedRemotingCallback syncHandler = new SynchronizedRemotingCallback();
		//pending and sending
		this.channelHandler.pending(this.channel, data, offset, length, syncHandler);

		synchronized (syncHandler.sync) {
			try {
				if (timeoutMillisecond > 0)
					syncHandler.sync.wait(timeoutMillisecond);
				else
					syncHandler.sync.wait();
			} catch (InterruptedException e) {
				this.channelHandler.cancel(syncHandler);
				throw new ChannelException("waiting callback error", e);
			}
		}

		if (syncHandler.isSucess())
			return syncHandler.getResult();

		if (syncHandler.getFailure() != null)
			throw new ChannelException("remoting call error", syncHandler.getFailure());
		if (timeoutMillisecond > 0)
			throw new ChannelException("remoting call timeout");

		throw new ChannelException("unknown error");
	}
}
