package com.taobao.top.link.channel.websocket;

import org.jboss.netty.channel.Channel;

import com.taobao.top.link.channel.ServerChannelSender;

public class WebSocketServerChannelSender extends WebSocketChannelSender implements ServerChannelSender {
	public WebSocketServerChannelSender(Channel channel) {
		super(channel);
	}

	public boolean isOpen() {
		return this.channel.isOpen();
	}
}
