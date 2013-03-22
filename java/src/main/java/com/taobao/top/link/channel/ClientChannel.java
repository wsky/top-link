package com.taobao.top.link.channel;

import java.net.URI;


public interface ClientChannel extends ChannelSender {
	public boolean isConnected();
	public void setChannelHandler(ChannelHandler handler);
	public void setUri(URI uri);
	public URI getUri();
}
