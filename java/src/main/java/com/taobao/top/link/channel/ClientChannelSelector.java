package com.taobao.top.link.channel;

import java.net.URI;

import com.taobao.top.link.Identity;

public interface ClientChannelSelector {
	public ClientChannel getChannel(URI uri,Identity identity) throws ChannelException;
	public void returnChannel(ClientChannel channel);
}