package com.taobao.top.link.remoting;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ClientChannelSelector;

public class RemotingService {
	private static LoggerFactory loggerFactory = DefaultLoggerFactory.getDefault();
	private static ClientChannelSelector channelSelector;
	private static RemotingClientChannelHandler channelHandler;

	protected static void setLoggerFactory(LoggerFactory loggerFactory) {
		RemotingService.loggerFactory = loggerFactory;
	}

	protected static void setChannelSelector(ClientChannelSelector selector) {
		channelSelector = selector;
	}

	public static Object connect(URI remoteUri, Class<?> interfaceClass) {
		return connect(remoteUri).create(interfaceClass, remoteUri);
	}

	public static DynamicProxy connect(URI remoteUri) {
		return new DynamicProxy(remoteUri, getChannelSelector(), getChannelHandler());
	}

	private synchronized static RemotingClientChannelHandler getChannelHandler() {
		if (channelHandler == null)
			channelHandler = new RemotingClientChannelHandler(loggerFactory, new AtomicInteger(0));
		return channelHandler;
	}

	private synchronized static ClientChannelSelector getChannelSelector() {
		if (channelSelector == null)
			channelSelector = new ClientChannelPooledSelector(loggerFactory);
		return channelSelector;
	}
}