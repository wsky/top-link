package com.taobao.top.link.channel.websocket;

import java.nio.ByteBuffer;

import org.jboss.netty.channel.ChannelHandlerContext;

import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ChannelSender;

public class WebSocketChannelSender extends WebSocketEndpointContext implements ChannelSender {

	public WebSocketChannelSender(ChannelHandlerContext ctx) {
		super(ctx);
	}

	@Override
	public void send(byte[] data, int offset, int length) throws ChannelException {
		this.reply(data, offset, length);
	}

	@Override
	public void send(ByteBuffer dataBuffer, SendHandler sendHandler) throws ChannelException {
		this.reply(dataBuffer, sendHandler);
	}
}
