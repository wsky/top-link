package com.taobao.top.link.endpoint;

import java.nio.ByteBuffer;

import com.taobao.top.link.channel.ChannelSender.SendHandler;

public abstract class EndpointContext {
	private Object message;

	public Object getMessage() {
		return this.message;
	}

	public abstract void reply(byte[] data, int offset, int length);

	public abstract void reply(ByteBuffer dataBuffer);

	public abstract void reply(ByteBuffer dataBuffer, SendHandler sendHandler);
}
