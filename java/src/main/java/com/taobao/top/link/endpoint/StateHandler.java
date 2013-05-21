package com.taobao.top.link.endpoint;

import com.taobao.top.link.channel.ChannelSender;

public interface StateHandler {
	public void onConnected(EndpointProxy endpoint, ChannelSender sender);
}