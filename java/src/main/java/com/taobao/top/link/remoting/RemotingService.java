package com.taobao.top.link.remoting;

import java.net.URI;
import java.util.concurrent.atomic.AtomicInteger;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ClientChannelSelector;

public class RemotingService {
	private static AtomicInteger flag = new AtomicInteger(0);
	private static LoggerFactory loggerFactory = new DefaultLoggerFactory();
	private static ClientChannelSelector channelSelector = new ClientChannelPooledSelector(loggerFactory);
	// TODO:shared handler or one handler per channel?
	private static RemotingClientChannelHandler channelHandler = new RemotingClientChannelHandler(loggerFactory, flag);

	// not understandable
	public static void setLoggerFactory(LoggerFactory loggerFactory) {
		RemotingService.loggerFactory = loggerFactory;
		channelSelector = new ClientChannelPooledSelector(loggerFactory);
		channelHandler = new RemotingClientChannelHandler(loggerFactory, flag);
	}

	public static void setChannelSelector(ClientChannelSelector selector) {
		channelSelector = selector;
	}

	public static Object connect(URI remoteUri, Class<?> interfaceClass) {
		return connect(remoteUri).create(interfaceClass, remoteUri);
	}

	public static DynamicProxy connect(URI remoteUri) {
		return new DynamicProxy(remoteUri, channelSelector, channelHandler);
	}
}