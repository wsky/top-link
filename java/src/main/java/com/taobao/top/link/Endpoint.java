package com.taobao.top.link;

import java.net.URI;

import com.taobao.top.link.handler.ChannelSelectHandler;
import com.taobao.top.link.handler.ReceiveHandler;

public class Endpoint {
	private ReceiveHandler receiveHandler;
	private EndpointProxyHolder endpointProxyHolder;

	private ServerChannel serverChannel;
	private ChannelSelectHandler channelSelectHandler;

	public Endpoint(Identity identity) {

	}

	public void setReceiveHandler(ReceiveHandler handler) {
		this.receiveHandler = handler;
	}

	public void bind(ServerChannel channel) {
		this.serverChannel = channel;
		this.serverChannel.run(this.receiveHandler);
	}

	public EndpointProxy getEndpoint(URI uri) {
		EndpointProxy proxy = this.endpointProxyHolder.get(uri);
		proxy.using(this.channelSelectHandler.getClientChannel(uri));
		return proxy;
	}
}
