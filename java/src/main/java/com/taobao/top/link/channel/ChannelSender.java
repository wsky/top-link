package com.taobao.top.link.channel;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public interface ChannelSender {
	public void send(byte[] data, int offset, int length) throws ChannelException;
	public void send(ByteBuffer dataBuffer, SendHandler sendHandler) throws ChannelException;
	public boolean sendSync(ByteBuffer dataBuffer, SendHandler sendHandler,int timeoutMilliseconds) throws ChannelException;
	public void close(String reason);
	
	// channel info
	public SocketAddress getLocalAddress();
	public SocketAddress getRemoteAddress();
	
	public interface SendHandler {
		public void onSendComplete(boolean success);
	}
}
