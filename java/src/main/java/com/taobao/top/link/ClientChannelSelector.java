package com.taobao.top.link;

import java.net.URI;

public interface ClientChannelSelector {
	public ClientChannel getChannel(URI uri,Identity identity) throws ChannelException;
	public void returnChannel(ClientChannel channel);
}