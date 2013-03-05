package com.taobao.top.link.remoting;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;

import remoting.protocol.NotSupportedException;
import remoting.protocol.tcp.TcpContentDelimiter;
import remoting.protocol.tcp.TcpOperations;
import remoting.protocol.tcp.TcpProtocolHandle;
import remoting.protocol.tcp.TcpTransportHeader;

import com.taobao.top.link.BufferManager;
import com.taobao.top.link.ChannelException;
import com.taobao.top.link.ClientChannel;
import com.taobao.top.link.ClientChannel.SendHandler;

public class DynamicProxy implements InvocationHandler {
	// do not make execution timeout
	private int defaultTimeout = 0;

	private ClientChannel channel;
	private RemotingClientChannelHandler channelHandler;

	protected DynamicProxy(ClientChannel channel, RemotingClientChannelHandler handler) {
		this.channel = channel;
		this.channelHandler = handler;
	}

	protected ClientChannel getChannel() {
		return this.channel;
	}

	// high-level remoting

	// eg. remote.rem
	private String uriString;

	protected Object create(Class<?> interfaceClass, URI remoteUri) {
		this.uriString = remoteUri.toString();
		return Proxy.newProxyInstance(
				interfaceClass.getClassLoader(),
				new Class[] { interfaceClass },
				this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MethodCall methodCall = new MethodCall();
		methodCall.Uri = this.uriString;
		methodCall.MethodName = method.getName();
		methodCall.TypeName = method.getDeclaringClass().getName();
		// do not support method overloaded currently
		methodCall.MethodSignature = null;
		methodCall.Args = args;
		return this.call(methodCall);
	}

	// low-level remoting

	public MethodResponse call(MethodCall methodCall) throws ChannelException {
		return this.call(methodCall, this.defaultTimeout);
	}

	public MethodResponse call(MethodCall methodCall, int executionTimeoutMillisecond) throws ChannelException {
		SynchronizedRemotingCallback syncCallback = new SynchronizedRemotingCallback();
		final ByteBuffer buffer = this.channelHandler.pending(this.channel, syncCallback);

		TcpProtocolHandle handle = new TcpProtocolHandle(buffer);
		handle.WritePreamble();
		handle.WriteMajorVersion();
		handle.WriteMinorVersion();
		handle.WriteOperation(TcpOperations.Request);
		handle.WriteContentDelimiter(TcpContentDelimiter.ContentLength);
		handle.WriteContentLength(1024);
		HashMap<String, Object> headers = new HashMap<String, Object>();
		headers.put(TcpTransportHeader.RequestUri, this.uriString);
		handle.WriteTransportHeaders(headers);
		handle.WriteContent(null);

		ByteBuffer ret = this.send(buffer, syncCallback, executionTimeoutMillisecond);
		handle = new TcpProtocolHandle(buffer);
		handle.ReadPreamble();
		handle.ReadMajorVersion();
		handle.ReadMinorVersion();
		handle.ReadOperation();
		handle.ReadContentDelimiter();
		handle.ReadContentLength();
		try {
			handle.ReadTransportHeaders();
		} catch (NotSupportedException e) {
			e.printStackTrace();
		}
		handle.ReadContent();
		return null;
	}

	public ByteBuffer send(byte[] data, int offset, int length) throws ChannelException {
		return this.send(data, offset, length, this.defaultTimeout);
	}

	public ByteBuffer send(byte[] data,
			int offset, int length, int executionTimeoutMillisecond) throws ChannelException {
		SynchronizedRemotingCallback syncCallback = new SynchronizedRemotingCallback();
		final ByteBuffer buffer = this.channelHandler.pending(this.channel, syncCallback);
		buffer.put(data, offset, length);
		return this.send(buffer, syncCallback, executionTimeoutMillisecond);
	}

	private ByteBuffer send(final ByteBuffer buffer,
			SynchronizedRemotingCallback syncCallback,
			int executionTimeoutMillisecond) throws ChannelException {
		// reset buffer limit and position for send
		buffer.flip();
		this.channel.send(buffer, new SendHandler() {
			@Override
			public void onSendComplete() {
				BufferManager.returnBuffer(buffer);
			}
		});

		// send and receive maybe fast enough
		if (syncCallback.isSucess())
			return syncCallback.getResult();

		int i = 0, wait = 100;
		while (true) {
			if (syncCallback.isSucess())
				return syncCallback.getResult();

			if (syncCallback.getFailure() != null)
				throw unexcepException(syncCallback, "remoting call error", syncCallback.getFailure());

			if (executionTimeoutMillisecond > 0 && (i++) * wait >= executionTimeoutMillisecond)
				throw unexcepException(syncCallback, "remoting execution timeout", null);

			if (!this.channel.isConnected())
				throw unexcepException(syncCallback, "channel broken with unknown error", null);

			synchronized (syncCallback.sync) {
				try {
					syncCallback.sync.wait(wait);
				} catch (InterruptedException e) {
					throw unexcepException(syncCallback, "waiting callback interrupted", e);
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
