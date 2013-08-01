package com.taobao.top.link.endpoint;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.Text;
import com.taobao.top.link.channel.ServerChannelSender;

public class SingleProxyStateHandler implements StateHandler {
	@Override
	public void onConnect(EndpointProxy endpoint, ChannelSenderWrapper sender) throws LinkException {
		if (!(sender.getChannelSender() instanceof ServerChannelSender))
			return;
		// FIXME hack here, maybe not alwasy ServerChannelSender
		ServerChannelSender serverSender = (ServerChannelSender) sender.getChannelSender();
		if (serverSender.getContext("__endpoint") != null)
			throw new LinkException(Text.E_SINGLE_ALLOW);
		serverSender.setContext("__endpoint", endpoint);
	}

}
