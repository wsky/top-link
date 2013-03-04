package com.taobao.top.link;

import java.nio.ByteBuffer;

public class EndpointProxy {
	private ClientChannel channel;

	protected void using(ClientChannel channel) {
		this.channel = channel;
	}

	public void send(byte[] data, int offset, int length) throws ChannelException {
		this.channel.send(data, offset, length);
	}

	public void send(ByteBuffer dataBuffer) throws ChannelException {
		this.channel.send(dataBuffer, null);
	}
}
