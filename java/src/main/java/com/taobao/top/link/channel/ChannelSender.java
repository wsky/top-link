package com.taobao.top.link.channel;

import java.nio.ByteBuffer;


public interface ChannelSender {
	public abstract void send(byte[] data, int offset, int length) throws ChannelException;
	public abstract void send(ByteBuffer dataBuffer, SendHandler sendHandler) throws ChannelException;

	public interface SendHandler {
		public void onSendComplete();
	}
}
