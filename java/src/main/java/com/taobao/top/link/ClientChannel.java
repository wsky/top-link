package com.taobao.top.link;

import com.taobao.top.link.handler.ChannelHandler;

public abstract class ClientChannel {
	
	protected abstract void setChannelHandler(ChannelHandler handler);
	
	protected abstract void connect();

	public abstract void send(byte[] data, int offset, int length);
}
