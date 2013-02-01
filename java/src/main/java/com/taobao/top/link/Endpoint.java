package com.taobao.top.link;

import java.net.URI;

import com.taobao.top.link.handler.ReceiveHandler;

public class Endpoint {
	private ReceiveHandler receiveHandler;
	private ConnectionHolder connectionHolder;
	private EndpointProxyHolder endpointProxyHolder;

	public Endpoint(Identity identity) {

	}

	public void setReceiveHandler(ReceiveHandler handler) {
		this.receiveHandler = receiveHandler;
	}

	public void bind(ServerChannel channel) {
		channel.run(this.connectionHolder, this.receiveHandler);
	}

	public EndpointProxy getEndpoint(URI uri) {
		Connection connection = this.connectionHolder.get(uri);
		EndpointProxy proxy = this.endpointProxyHolder.get(uri);
		proxy.using(connection);
		return proxy;
	}
}
