package com.taobao.top.link;

import com.taobao.top.link.handler.ChannelHandler;

public abstract class ServerChannel {
	protected Endpoint endpoint;

	protected ChannelHandler getChannelHandler() {
		return this.endpoint.getChannelHandler();
	}

	protected void run(Endpoint endpoint) {
		this.endpoint = endpoint;
		this.run();
	}

	protected void onConnect(byte[] data,
			int offset, int length, ClientChannel clientChannel) {
		// resolve id
		Identity identity = this.getChannelHandler().receiveHandshake(data, offset, length);
		clientChannel.setUri(identity.getUri());

		// store proxy
		EndpointProxy proxy = new EndpointProxy(identity);
		this.endpoint.getEndpointProxyHolder().put(clientChannel.getUri(), proxy);

		// return self id
		// byte[] idData = this.endpoint.getIdentity().getData();
		// if (idData != null && idData.length > 0)
		// clientChannel.send(idData, 0, idData.length);
	}

	protected abstract void run();
}
