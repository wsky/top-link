package com.taobao.top.link;

import java.net.URI;

import com.taobao.top.link.handler.ChannelHandler;

public interface ClientChannel extends ChannelSender {
	public boolean isConnected();
	public void setChannelHandler(ChannelHandler handler);
	public void setUri(URI uri);
	public URI getUri();
}
