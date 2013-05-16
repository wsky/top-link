package com.taobao.top.link.remoting;

import java.util.concurrent.ExecutorService;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ClientChannelSelector;
import com.taobao.top.link.channel.ServerChannel;
import com.taobao.top.link.channel.websocket.WebSocketServerChannel;

// combined client/server remoting config helper
public class RemotingConfiguration {
	private static RemotingConfiguration configuration;

	public synchronized static RemotingConfiguration configure() {
		if (configuration == null)
			configuration = new RemotingConfiguration();
		return configuration;
	}

	private LoggerFactory loggerFactory;
	private DefaultRemotingServerChannelHandler defaultHandler;
	private Serializer serializer;

	public RemotingConfiguration() {
		this.loggerFactory(DefaultLoggerFactory.getDefault());
	}

	// should be set first
	public RemotingConfiguration loggerFactory(LoggerFactory loggerFactory) {
		this.loggerFactory = loggerFactory;
		RemotingService.setLoggerFactory(loggerFactory);
		return this;
	}

	public RemotingConfiguration clientChannelSelector(ClientChannelSelector selector) {
		RemotingService.setChannelSelector(selector);
		return this;
	}

	// TODO:refact to sink desgion
	public RemotingConfiguration serializer(Serializer serializer) {
		this.serializer = serializer;
		RemotingService.setSerializer(serializer);
		return this;
	}

	// shold be set before bind()
	public RemotingConfiguration defaultServerChannelHandler(DefaultRemotingServerChannelHandler channelHandler) {
		this.defaultHandler = channelHandler;
		return this;
	}

	// bind to custom channel
	public RemotingConfiguration bind(ServerChannel channel) {
		channel.setChannelHandler(this.getChannelHandler());
		channel.run();
		return this;
	}

	public RemotingConfiguration websocket(int port) {
		return this.bind(new WebSocketServerChannel(this.loggerFactory, port, true));
	}

	public RemotingConfiguration tcp(int port) {
		return this;
	}

	public RemotingConfiguration http(int port) {
		return this;
	}

	public RemotingConfiguration addProcessor(
			String objectUri, MethodCallProcessor processor) {
		this.defaultHandler.addProcessor(objectUri, processor);
		return this;
	}

	public RemotingConfiguration businessThreadPool(ExecutorService threadPool) {
		this.defaultHandler.setThreadPool(threadPool);
		return this;
	}

	private synchronized DefaultRemotingServerChannelHandler getChannelHandler() {
		if (this.defaultHandler == null)
			this.defaultHandler = new DefaultRemotingServerChannelHandler(this.loggerFactory);
		if (this.serializer != null)
			this.defaultHandler.setSerializer(this.serializer);
		return this.defaultHandler;
	}
}
