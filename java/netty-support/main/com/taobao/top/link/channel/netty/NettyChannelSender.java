package com.taobao.top.link.channel.netty;

import org.jboss.netty.channel.Channel;

import com.taobao.top.link.channel.ChannelSender;

public abstract class NettyChannelSender implements ChannelSender {
	protected Channel channel;

	public NettyChannelSender(Channel channel) {
		this.channel = channel;
	}
	
	public Channel getChannel() {
		return this.channel;
	}
}