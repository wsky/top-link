package com.taobao.top.link;

import java.net.URI;

import com.taobao.top.link.handler.ChannelHandler;
import com.taobao.top.link.handler.ChannelSelectHandler;

public class Endpoint {
	private EndpointProxyHolder endpointProxyHolder;

	private Identity identity;
	private ServerChannel serverChannel;
	private ChannelSelectHandler channelSelectHandler;
	private ChannelHandler channelHandler;

	public Endpoint(Identity identity) {
		this.identity = identity;
	}

	public void setChannelHandler(ChannelHandler handler) {
		this.channelHandler = handler;
	}

	public void bind(ServerChannel channel) {
		this.serverChannel = channel;
		this.serverChannel.run(this.channelHandler);
	}

	public EndpointProxy getEndpoint(URI uri) {
		EndpointProxy proxy = this.endpointProxyHolder.get(uri);
		ClientChannel channel = this.channelSelectHandler.getClientChannel(uri);
		channel.setChannelHandler(this.channelHandler);
		proxy.using(channel);
		return proxy;
	}
}
