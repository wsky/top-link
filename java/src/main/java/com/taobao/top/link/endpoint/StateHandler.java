package com.taobao.top.link.endpoint;

import com.taobao.top.link.channel.ServerChannelSender;

public interface StateHandler {
	public void onConnected(EndpointProxy endpoint, ServerChannelSender sender);
}