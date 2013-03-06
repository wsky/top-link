package com.taobao.top.link.remoting;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;

import remoting.protocol.tcp.TcpOperations;
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
		this.uriString = "";
	}

	protected ClientChannel getChannel() {
		return this.channel;
	}

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

		MethodReturn methodReturn = this.invoke(methodCall);

		if (methodReturn.Exception == null)
			return methodReturn.ReturnValue;

		throw methodReturn.Exception;
	}

	public MethodReturn invoke(MethodCall methodCall) throws RemotingException, FormatterException {
		return this.invoke(methodCall, this.defaultTimeout);
	}

	public MethodReturn invoke(MethodCall methodCall, int executionTimeoutMillisecond) throws RemotingException, FormatterException {
		SynchronizedRemotingCallback syncCallback = new SynchronizedRemotingCallback();

		HashMap<String, Object> transportHeaders = new HashMap<String, Object>();
		transportHeaders.put(TcpTransportHeader.RequestUri, this.uriString);

		return this.send(this.channelHandler.pending(syncCallback,
				TcpOperations.Request,
				transportHeaders,
				methodCall),
				syncCallback,
				executionTimeoutMillisecond);
	}

	private MethodReturn send(final ByteBuffer buffer,
			SynchronizedRemotingCallback syncCallback,
			int executionTimeoutMillisecond) throws RemotingException {
		// reset buffer limit and position for send
		buffer.flip();
		
		try {
			this.channel.send(buffer, new SendHandler() {
				@Override
				public void onSendComplete() {
					BufferManager.returnBuffer(buffer);
				}
			});
		} catch (ChannelException e) {
			throw unexcepException(syncCallback, "send error", e);
		}

		// send and receive maybe fast enough
		if (syncCallback.isSucess())
			return syncCallback.getMethodReturn();

		int i = 0, wait = 100;
		while (true) {
			if (syncCallback.isSucess())
				return syncCallback.getMethodReturn();

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

	private RemotingException unexcepException(
			SynchronizedRemotingCallback callback, String message, Throwable innerException) {
		this.channelHandler.cancel(callback);
		return innerException != null
				? new RemotingException(message, innerException)
				: new RemotingException(message);
	}
}
