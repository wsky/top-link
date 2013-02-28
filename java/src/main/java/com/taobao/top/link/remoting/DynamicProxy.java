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

	public ByteBuffer call(byte[] data, int offset, int length) throws ChannelException {
		return this.call(data, offset, length, 0);
	}

	public ByteBuffer call(byte[] data, int offset, int length, int timeoutMillisecond) throws ChannelException {
		SynchronizedRemotingCallback syncHandler = new SynchronizedRemotingCallback();

		// pending and sending
		this.channelHandler.pending(this.channel, data, offset, length, syncHandler);

		// send and receive maybe fast enough
		if (syncHandler.isSucess())
			return syncHandler.getResult();

		int i = 0, wait = 100;
		while (true) {
			if (syncHandler.isSucess())
				return syncHandler.getResult();

			if (syncHandler.getFailure() != null)
				throw unexcepException(syncHandler, "remoting call error", syncHandler.getFailure());

			if (timeoutMillisecond > 0 && (i++) * wait >= timeoutMillisecond)
				throw unexcepException(syncHandler, "remoting call timeout", null);

			if (!this.channel.isConnected())
				throw unexcepException(syncHandler, "channel broken with unknown error", null);

			synchronized (syncHandler.sync) {
				try {
					syncHandler.sync.wait(wait);
				} catch (InterruptedException e) {
					throw unexcepException(syncHandler, "waiting callback interrupted", e);
				}
			}
		}
	}

	private ChannelException unexcepException(
			SynchronizedRemotingCallback callback, String message, Throwable innerException) {
		this.channelHandler.cancel(callback);
		return innerException != null
				? new ChannelException(message, innerException)
				: new ChannelException(message);
	}
}
