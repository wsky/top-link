package com.taobao.top.link.endpoint;

import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ChannelContext;
import com.taobao.top.link.channel.ChannelHandler;

// make timing
public class EndpointChannelHandler implements ChannelHandler {
	private Logger logger;
	private Endpoint endpoint;

	public EndpointChannelHandler(LoggerFactory loggerFactory, Endpoint endpoint) {
		this.logger = loggerFactory.create(this);
		this.endpoint = endpoint;
	}

	@Override
	public void onConnect(ChannelContext context) {

	}

	@Override
	public void onMessage(ChannelContext context) throws Exception {
		if (this.endpoint.getMessageHandler() != null)
			this.endpoint.getMessageHandler().onMessage(new EndpointContext(context));
	}

	@Override
	public void onError(ChannelContext context) throws Exception {
		this.logger.error("channel error", context.getError());
	}

}
