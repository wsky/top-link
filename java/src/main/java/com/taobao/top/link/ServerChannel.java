package com.taobao.top.link;

public abstract class ServerChannel {
	protected Endpoint endpoint;

	protected void run(Endpoint endpoint) {
		this.endpoint = endpoint;
		this.run();
	}

	protected void onConnect(byte[] data,
			int offset, int length, ClientChannel clientChannel) {
		// resolve id
		Identity identity = this.endpoint.getChannelHandler().receiveHandshake(data, offset, length);
		clientChannel.setUri(identity.getUri());

		// store proxy
		EndpointProxy proxy = new EndpointProxy(identity);
		this.endpoint.getEndpointProxyHolder().put(clientChannel.getUri(), proxy);

		// return self id
		// byte[] idData = this.endpoint.getIdentity().getData();
		// if (idData != null && idData.length > 0)
		// clientChannel.send(idData, 0, idData.length);
	}

	protected void onReceive(byte[] data,
			int offset, int length, ClientChannel clientChannel) {
		this.endpoint.getChannelHandler().onReceive(data,
				offset, length, this.endpoint.getEndpoint(clientChannel));
	}

	protected abstract void run();
}
