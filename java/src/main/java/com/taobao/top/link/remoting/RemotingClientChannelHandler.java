package com.taobao.top.link.remoting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import remoting.protocol.NotSupportedException;
import remoting.protocol.tcp.TcpContentDelimiter;
import remoting.protocol.tcp.TcpOperations;
import remoting.protocol.tcp.TcpProtocolHandle;
import remoting.protocol.tcp.TcpTransportHeader;

import com.taobao.top.link.BufferManager;
import com.taobao.top.link.EndpointContext;
import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.handler.ChannelHandler;

public class RemotingClientChannelHandler extends ChannelHandler {
	private Logger logger;
	private AtomicInteger flagAtomic;
	private HashMap<String, RemotingCallback> callbacks = new HashMap<String, RemotingCallback>();

	public RemotingClientChannelHandler(LoggerFactory loggerFactory, AtomicInteger flagAtomic) {
		this.logger = loggerFactory.create(this);
		this.flagAtomic = flagAtomic;
	}

	// act as formatter sink
	public ByteBuffer pending(RemotingCallback handler, short operation,
			HashMap<String, Object> transportHeaders, MethodCall methodCall) throws FormatterException {
		byte[] data = this.serializeMethodCall(methodCall);
		String flag = Integer.toString(this.flagAtomic.incrementAndGet());

		ByteBuffer requestBuffer = BufferManager.getBuffer();
		TcpProtocolHandle handle = new TcpProtocolHandle(requestBuffer);
		handle.WritePreamble();
		handle.WriteMajorVersion();
		handle.WriteMinorVersion();
		handle.WriteOperation(TcpOperations.Request);
		handle.WriteContentDelimiter(TcpContentDelimiter.ContentLength);
		handle.WriteContentLength(data.length);
		transportHeaders.put(RemotingTransportHeader.Flag, flag);
		handle.WriteTransportHeaders(transportHeaders);
		handle.WriteContent(data);
		
		handler.flag = flag;
		this.callbacks.put(handler.flag, handler);// concurrent?
		this.logger.debug("pending methodCall#%s", flag);

		return requestBuffer;
	}

	public void cancel(RemotingCallback callback) {
		this.callbacks.remove(callback.flag);
	}

	@Override
	public void onReceive(ByteBuffer dataBuffer, EndpointContext context) {
		TcpProtocolHandle protocol = new TcpProtocolHandle(dataBuffer);
		protocol.ReadPreamble();
		protocol.ReadMajorVersion();
		protocol.ReadMinorVersion();

		short operation = protocol.ReadOperation();
		if (operation != TcpOperations.Reply)
			return;

		protocol.ReadContentDelimiter();
		protocol.ReadContentLength();

		HashMap<String, Object> transportHeaders = null;
		try {
			transportHeaders = protocol.ReadTransportHeaders();
		} catch (NotSupportedException e) {
			e.printStackTrace();
		}
		Object flag;
		if (transportHeaders == null ||
				(flag = transportHeaders.get(RemotingTransportHeader.Flag)) == null)
			return;

		this.logger.debug("receive methodReturn of methodCall#%s", flag);

		RemotingCallback handler = this.callbacks.remove(flag);
		if (handler == null)
			return;

		Object statusCode = transportHeaders.get(TcpTransportHeader.StatusCode);
		Object statusPhrase = transportHeaders.get(TcpTransportHeader.StatusPhrase);
		if (statusCode != null &&
				Integer.parseInt(statusCode.toString()) > 0) {
			handler.onException(statusPhrase != null ?
					new Exception(String.format("remote reutrn error#%s: %s" + statusCode + statusPhrase)) :
					new Exception("remote reutrn unknow error#" + statusCode));
			return;
		}

		MethodReturn methodReturn = null;
		try {
			methodReturn = this.deserializeMethodReturn(protocol.ReadContent());
		} catch (FormatterException e) {
			handler.onException(e);
			return;
		}

		try {
			handler.onMethodReturn(methodReturn);
		} catch (Exception e) {
			e.printStackTrace();
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

	private byte[] serializeMethodCall(MethodCall methodCall) throws FormatterException {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(methodCall);
			return bos.toByteArray();
		} catch (Exception e) {
			throw new FormatterException("serialize MethodCall error", e);
		}
	}

	private MethodReturn deserializeMethodReturn(byte[] input) throws FormatterException {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(input);
			ObjectInputStream ois = new ObjectInputStream(bis);
			return (MethodReturn) ois.readObject();
		} catch (Exception e) {
			throw new FormatterException("deserialize MethodReturn error", e);
		}
	}
}
