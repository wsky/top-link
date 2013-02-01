package com.taobao.top.link.handler;

import java.net.URI;

import com.taobao.top.link.ClientChannel;

public interface ChannelSelectHandler {
	ClientChannel getClientChannel(URI uri);
}
