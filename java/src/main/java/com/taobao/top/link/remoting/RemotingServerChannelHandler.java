package com.taobao.top.link.remoting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;

import remoting.protocol.NotSupportedException;
import remoting.protocol.tcp.TcpContentDelimiter;
import remoting.protocol.tcp.TcpOperations;
import remoting.protocol.tcp.TcpProtocolHandle;
import remoting.protocol.tcp.TcpTransportHeader;

import com.taobao.top.link.BufferManager;
import com.taobao.top.link.ClientChannel.SendHandler;
import com.taobao.top.link.EndpointContext;
import com.taobao.top.link.handler.SimpleChannelHandler;

public abstract class RemotingServerChannelHandler extends SimpleChannelHandler {
	@Override
	public void onReceive(ByteBuffer dataBuffer, EndpointContext context) {		
		TcpProtocolHandle protocol = new TcpProtocolHandle(dataBuffer);
		protocol.ReadPreamble();
		protocol.ReadMajorVersion();
		protocol.ReadMinorVersion();
		short operation = protocol.ReadOperation();
		protocol.ReadContentDelimiter();
		protocol.ReadContentLength();

		HashMap<String, Object> transportHeaders = null;
		try {
			transportHeaders = protocol.ReadTransportHeaders();
		} catch (NotSupportedException e) {
			e.printStackTrace();
			return;
		}
		Object flag = transportHeaders.get(RemotingTransportHeader.Flag);
		transportHeaders.clear();
		transportHeaders.put(RemotingTransportHeader.Flag, flag);
		System.out.println("receive methodCall#" + flag);

		// get method return
		MethodCall methodCall = null;
		MethodReturn methodReturn = null;
		try {
			methodCall = this.deserializeMethodCall(protocol.ReadContent());
			methodReturn = this.onMethodCall(methodCall);
		} catch (Exception e) {
			methodReturn = new MethodReturn();
			methodReturn.Exception = e;
		}

		// oneway?
		if (operation == TcpOperations.OneWayRequest)
			return;

		byte[] data;
		try {
			data = this.serializeMethodReturn(methodReturn);
		} catch (FormatterException e) {
			data = new byte[0];
			transportHeaders.put(TcpTransportHeader.StatusCode, 400);
			transportHeaders.put(TcpTransportHeader.StatusPhrase, e.getMessage());
		}

		final ByteBuffer responseBuffer = BufferManager.getBuffer();
		TcpProtocolHandle handle = new TcpProtocolHandle(responseBuffer);
		handle.WritePreamble();
		handle.WriteMajorVersion();
		handle.WriteMinorVersion();
		handle.WriteOperation(TcpOperations.Reply);
		handle.WriteContentDelimiter(TcpContentDelimiter.ContentLength);
		handle.WriteContentLength(data.length);
		handle.WriteTransportHeaders(transportHeaders);
		handle.WriteContent(data);

		responseBuffer.flip();
		context.reply(responseBuffer, new SendHandler() {
			@Override
			public void onSendComplete() {
				BufferManager.returnBuffer(responseBuffer);
			}
		});
	}

	public abstract MethodReturn onMethodCall(MethodCall methodCall);

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
