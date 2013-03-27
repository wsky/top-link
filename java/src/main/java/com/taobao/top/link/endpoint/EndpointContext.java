package com.taobao.top.link.endpoint;

import java.nio.ByteBuffer;

import com.taobao.top.link.channel.ChannelContext;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ChannelSender.SendHandler;

public class EndpointContext {
	private ChannelContext channelContext;

	public EndpointContext(ChannelContext channelContext) {
		this.channelContext = channelContext;
	}

	public Object getMessage() {
		return this.channelContext.getMessage();
	}

	public void reply(byte[] data, int offset, int length) throws ChannelException {
		this.channelContext.reply(data, offset, length);
	}

	public void reply(ByteBuffer dataBuffer) throws ChannelException {
		this.channelContext.reply(dataBuffer);
	}

	public void reply(ByteBuffer dataBuffer, SendHandler sendHandler) {
		this.reply(dataBuffer, sendHandler);
	}
}
