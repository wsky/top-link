package com.taobao.top.link;

import java.net.URI;

import com.taobao.top.link.handler.ChannelHandler;
import com.taobao.top.link.handler.ChannelSelectHandler;
import com.taobao.top.link.websocket.WebSocketChannelSelectHandler;

public class Endpoint {
	private EndpointProxyHolder endpointProxyHolder;

	private Identity identity;
	private ServerChannel serverChannel;
	private ChannelSelectHandler channelSelectHandler;
	private ChannelHandler channelHandler;

	public Endpoint(Identity identity) {
		this.identity = identity;
		this.channelSelectHandler = new WebSocketChannelSelectHandler(this);
	}

	public Identity getIdentity() {
		return this.identity;
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

	protected EndpointProxyHolder getEndpointProxyHolder() {
		return this.endpointProxyHolder;
	}

	protected EndpointProxy getEndpoint(ClientChannel channel) {
		EndpointProxy proxy = new EndpointProxy();
		// this.endpointProxyHolder.get(channel.getUri());
		channel.setChannelHandler(this.channelHandler);
		proxy.using(channel);
		return proxy;
	}
}
