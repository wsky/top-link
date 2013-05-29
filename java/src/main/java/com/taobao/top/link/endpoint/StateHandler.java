package com.taobao.top.link.endpoint;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.channel.ServerChannelSender;

public interface StateHandler {
	public void onConnect(EndpointProxy endpoint, ServerChannelSender sender) throws LinkException;
}