package com.taobao.top.link.remoting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import remoting.protocol.NotSupportedException;
import remoting.protocol.tcp.TcpContentDelimiter;
import remoting.protocol.tcp.TcpOperations;
import remoting.protocol.tcp.TcpProtocolHandle;
import remoting.protocol.tcp.TcpTransportHeader;

import com.taobao.top.link.BufferManager;
import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ChannelContext;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.SimpleChannelHandler;
import com.taobao.top.link.channel.ChannelSender.SendHandler;

public abstract class RemotingServerChannelHandler extends SimpleChannelHandler {
	protected Logger logger;
	private ExecutorService threadPool;

	public RemotingServerChannelHandler(LoggerFactory loggerFactory) {
		this.logger = loggerFactory.create(this);
		this.setThreadPool(new ThreadPoolExecutor(
				20, 100, 300, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()));
	}

	public void setThreadPool(ExecutorService threadPool) {
		this.threadPool = threadPool;
	}

	public abstract MethodReturn onMethodCall(MethodCall methodCall) throws Throwable;

	@Override
	public void onMessage(final ChannelContext context) throws ChannelException, NotSupportedException {
		final TcpProtocolHandle protocol = new TcpProtocolHandle((ByteBuffer) context.getMessage());
		protocol.ReadPreamble();
		protocol.ReadMajorVersion();
		protocol.ReadMinorVersion();
		final short operation = protocol.ReadOperation();
		protocol.ReadContentDelimiter();
		protocol.ReadContentLength();
		final HashMap<String, Object> transportHeaders = protocol.ReadTransportHeaders();
		Object flag = transportHeaders.get(RemotingTransportHeader.Flag);
		transportHeaders.clear();
		transportHeaders.put(RemotingTransportHeader.Flag, flag);

		// just use netty io-woker thread, count=cpucore
		if (this.threadPool == null) {
			this.internalOnMessage(context, protocol, operation, transportHeaders);
			return;
		}

		// dispatch to business workers
		try {
			this.threadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						internalOnMessage(context, protocol, operation, transportHeaders);
					} catch (ChannelException e) {
						logger.error(e);
					}
				}
			});
		} catch (RejectedExecutionException exception) {
			String statusPhrase = String.format(
					"server threadpool full, threadpool maxsize is: %s",
					((ThreadPoolExecutor) this.threadPool).getMaximumPoolSize());
			this.logger.fatal(statusPhrase);
			transportHeaders.put(TcpTransportHeader.StatusCode, 500);
			transportHeaders.put(TcpTransportHeader.StatusPhrase, statusPhrase);
			this.reply(context, transportHeaders, null);
		}
	}

	private void internalOnMessage(ChannelContext context,
			TcpProtocolHandle protocol,
			short operation,
			HashMap<String, Object> transportHeaders) throws ChannelException {
		// get method return
		MethodCall methodCall = null;
		MethodReturn methodReturn = null;
		try {
			methodCall = this.deserializeMethodCall(protocol.ReadContent());
			methodReturn = this.onMethodCall(methodCall);
		} catch (Throwable e) {
			methodReturn = new MethodReturn();
			methodReturn.Exception = e;
		}

		// oneway?
		if (operation == TcpOperations.OneWayRequest)
			return;

		byte[] data = null;
		try {
			data = this.serializeMethodReturn(methodReturn);
		} catch (FormatterException e) {
			transportHeaders.put(TcpTransportHeader.StatusCode, 400);
			transportHeaders.put(TcpTransportHeader.StatusPhrase, e.getMessage());
		}
		this.reply(context, transportHeaders, data);
	}

	private void reply(ChannelContext context,
			HashMap<String, Object> transportHeaders,
			byte[] data) throws ChannelException {
		final ByteBuffer responseBuffer = BufferManager.getBuffer();
		TcpProtocolHandle handle = new TcpProtocolHandle(responseBuffer);
		handle.WritePreamble();
		handle.WriteMajorVersion();
		handle.WriteMinorVersion();
		handle.WriteOperation(TcpOperations.Reply);
		handle.WriteContentDelimiter(TcpContentDelimiter.ContentLength);
		handle.WriteContentLength(data != null ? data.length : 0);
		handle.WriteTransportHeaders(transportHeaders);
		if (data != null)
			handle.WriteContent(data);

		responseBuffer.flip();
		context.reply(responseBuffer, new SendHandler() {
			@Override
			public void onSendComplete() {
				BufferManager.returnBuffer(responseBuffer);
			}
		});
	}

	private byte[] serializeMethodReturn(MethodReturn methodReturn) throws FormatterException {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(methodReturn);
			return bos.toByteArray();
		} catch (Exception e) {
			throw new FormatterException("serialize MethodReturn error", e);
		}
	}

	private MethodCall deserializeMethodCall(byte[] input) throws FormatterException {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(input);
			ObjectInputStream ois = new ObjectInputStream(bis);
			return (MethodCall) ois.readObject();
		} catch (Exception e) {
			throw new FormatterException("deserialize MethodCall error", e);
		}
	}
}
