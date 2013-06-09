package com.taobao.top.link.channel.tcp;

import org.jboss.netty.channel.group.ChannelGroup;

import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ChannelHandler;

public class TcpServerUpstreamHandlerWrapper extends TcpServerUpstreamHandler {
	public TcpServerUpstreamHandlerWrapper(LoggerFactory loggerFactory, ChannelHandler channelHandler, ChannelGroup channelGroup) {
		super(loggerFactory, channelHandler, channelGroup);
	}
}
