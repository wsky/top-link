package com.taobao.top.link;

import com.taobao.top.link.handler.ChannelHandler;

public class EndpointProxy {
	private ClientChannel channel;
	
	protected void using(ClientChannel channel) {
		this.channel = channel;
	}

	public void send(byte[] data, int offset, int length) throws ChannelException {
		this.channel.send(data, offset, length);
	}

	// special once-handle
	public void send(byte[] data, int offset, int length, ChannelHandler handler) {
		
	}
}
