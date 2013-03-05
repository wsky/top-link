package com.taobao.top.link.remoting;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;

import com.taobao.top.link.BufferManager;
import com.taobao.top.link.ChannelException;
import com.taobao.top.link.ClientChannel;
import com.taobao.top.link.ClientChannel.SendHandler;

public class DynamicProxy implements InvocationHandler {
	private ClientChannel channel;
	private RemotingClientChannelHandler channelHandler;

	protected DynamicProxy(ClientChannel channel, RemotingClientChannelHandler handler) {
		this.channel = channel;
		this.channelHandler = handler;
	}
	
	protected ClientChannel getChannel() {
		return this.channel;
	}

	protected Object create(Class<?> interfaceClass) {
		return Proxy.newProxyInstance(
				interfaceClass.getClassLoader(),
				new Class[] { interfaceClass },
				this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// TODO:resolve response by sink
		return null;
	}

	public ByteBuffer send(byte[] data, int offset, int length) throws ChannelException {
		return this.send(data, offset, length, 0);
	}

	public ByteBuffer send(byte[] data,
			int offset, int length, int timeoutMillisecond) throws ChannelException {
		SynchronizedRemotingCallback syncHandler = new SynchronizedRemotingCallback();

		// pending and sending
		final ByteBuffer buffer = this.channelHandler.pending(this.channel, syncHandler);
		buffer.put(data, offset, length);
		buffer.flip();
		this.channel.send(buffer, new SendHandler() {
			@Override
			public void onSendComplete() {
				BufferManager.returnBuffer(buffer);
			}
		});

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
