package com.taobao.top.link.websocket;

import java.nio.ByteBuffer;

import org.jboss.netty.channel.ChannelHandlerContext;

import com.taobao.top.link.ChannelException;
import com.taobao.top.link.ChannelSender;

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
