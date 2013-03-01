package com.taobao.top.link.remoting;

import java.net.URI;

import com.taobao.top.link.ChannelException;
import com.taobao.top.link.ClientChannel;
import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.websocket.WebSocketChannelSelectHandler;

public class RemotingService {
	private static LoggerFactory loggerFactory = new DefaultLoggerFactory();
	private static WebSocketChannelSelectHandler selectHandler = new WebSocketChannelSelectHandler(new DefaultLoggerFactory());

	public static DynamicProxy connect(URI uri) throws ChannelException {
		ClientChannel channel = selectHandler.getClientChannel(uri);
		RemotingClientChannelHandler channelHandler = new RemotingClientChannelHandler(
				loggerFactory.create("RemotingClientChannelHandler-" + uri));
		channel.setChannelHandler(channelHandler);
		return new DynamicProxy(channel, channelHandler);
	}

	public static Object connect(URI uri, Class<?> interfaceClass) throws ChannelException {
		ClientChannel channel = selectHandler.getClientChannel(uri);
		RemotingClientChannelHandler channelHandler = new RemotingClientChannelHandler(
				loggerFactory.create("RemotingClientChannelHandler-" + uri));
		channel.setChannelHandler(channelHandler);
		return new DynamicProxy(channel, channelHandler).create(interfaceClass);
	}
}