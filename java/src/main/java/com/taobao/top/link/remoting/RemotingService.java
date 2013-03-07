package com.taobao.top.link.remoting;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import com.taobao.top.link.ChannelException;
import com.taobao.top.link.ClientChannel;
import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.websocket.WebSocketChannelSelectHandler;

public class RemotingService {
	private static AtomicInteger flag = new AtomicInteger(0);
	private static LoggerFactory loggerFactory = new DefaultLoggerFactory();
	private static WebSocketChannelSelectHandler selectHandler = new WebSocketChannelSelectHandler(loggerFactory);
	// TODO:shared handler or one handler per channel?
	private static RemotingClientChannelHandler channelHandler = new RemotingClientChannelHandler(loggerFactory, flag);

	public static Object connect(URI remoteUri, Class<?> interfaceClass) throws ChannelException {
		return connect(remoteUri).create(interfaceClass, remoteUri);
	}

	public static DynamicProxy connect(URI remoteUri) throws ChannelException {
		return proxy(selectHandler.getClientChannel(remoteUri), remoteUri);
	}

	protected static DynamicProxy proxy(ClientChannel channel) {
		return proxy(channel, null);
	}

	protected static DynamicProxy proxy(ClientChannel channel, URI remoteUri) {
		channel.setChannelHandler(channelHandler);
		return new DynamicProxy(remoteUri, channel, channelHandler);
	}
}