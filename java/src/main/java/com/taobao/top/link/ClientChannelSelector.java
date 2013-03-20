package com.taobao.top.link;

import java.net.URI;

public interface ClientChannelSelector {
	public ClientChannel getClientChannel(URI uri) throws ChannelException;
}