package com.taobao.top.link.handler;

import java.net.URI;

import com.taobao.top.link.ChannelException;
import com.taobao.top.link.ClientChannel;

public interface ChannelSelectHandler {
	public ClientChannel getClientChannel(URI uri) throws ChannelException;
}