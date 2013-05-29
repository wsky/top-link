package com.taobao.top.link.endpoint;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.Text;
import com.taobao.top.link.channel.ServerChannelSender;

public class SingleProxyStateHandler implements StateHandler {
	@Override
	public void onConnect(EndpointProxy endpoint, ServerChannelSender sender) throws LinkException {
		if (sender.getContext("__endpoint") != null)
			throw new LinkException(Text.E_SINGLE_ALLOW);
		sender.setContext("__endpoint", endpoint);
	}

}
