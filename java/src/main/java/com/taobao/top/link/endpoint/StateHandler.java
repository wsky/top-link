package com.taobao.top.link.endpoint;

import com.taobao.top.link.LinkException;

public interface StateHandler {
	public void onConnect(EndpointProxy endpoint, ChannelSenderWrapper sender) throws LinkException;
}