package com.taobao.top.link;

import java.nio.ByteBuffer;

import com.taobao.top.link.handler.ChannelHandler;

public abstract class ClientChannel {

	public abstract boolean isConnected();

	public abstract void setChannelHandler(ChannelHandler handler);

	protected abstract void addOnceChannelHandler(ChannelHandler handler);

	public abstract void send(byte[] data, int offset, int length) throws ChannelException;

	public abstract void send(ByteBuffer dataBuffer, SendHandler sendHandler) throws ChannelException;

	public interface SendHandler {
		public void onSendComplete();
	}
}
