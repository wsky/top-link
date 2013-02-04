package com.taobao.top.link;

import java.net.URI;

import com.taobao.top.link.handler.ChannelHandler;

public abstract class ClientChannel {

	protected abstract void setChannelHandler(ChannelHandler handler);

	protected abstract void connect();

	public abstract void setUri(URI uri);

	public abstract URI getUri();

	public abstract void send(byte[] data, int offset, int length);
}
