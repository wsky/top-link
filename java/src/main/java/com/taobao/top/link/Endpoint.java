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
		this.serverChannel.run(this);
	}

	public EndpointProxy getEndpoint(URI uri) throws ChannelException {
		return this.getEndpoint(this.channelSelectHandler.getClientChannel(uri));
	}

	protected ChannelHandler getChannelHandler() {
		return this.channelHandler;
	}

	protected Identity getIdentity() {
		return this.identity;
	}

	protected EndpointProxyHolder getEndpointProxyHolder() {
		return this.endpointProxyHolder;
	}

	protected EndpointProxy getEndpoint(ClientChannel channel) {
		EndpointProxy proxy = this.endpointProxyHolder.get(channel.getUri());
		channel.setChannelHandler(this.channelHandler);
		proxy.using(channel);
		return proxy;
	}
}
