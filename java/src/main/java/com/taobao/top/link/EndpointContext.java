package com.taobao.top.link;

import java.nio.ByteBuffer;

import com.taobao.top.link.ChannelSender.SendHandler;

public abstract class EndpointContext {
	public abstract void reply(byte[] data, int offset, int length);
	public abstract void reply(ByteBuffer dataBuffer);
	public abstract void reply(ByteBuffer dataBuffer, SendHandler sendHandler);
}
