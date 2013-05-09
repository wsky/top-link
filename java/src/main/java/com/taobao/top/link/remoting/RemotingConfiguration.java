package com.taobao.top.link.remoting;

import java.util.concurrent.ExecutorService;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.LoggerFactory;
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

	public RemotingConfiguration() {
		this.loggerFactory(DefaultLoggerFactory.getDefault());
	}

	// should be set first
	public RemotingConfiguration loggerFactory(LoggerFactory loggerFactory) {
		this.loggerFactory = loggerFactory;
		this.defaultHandler = new DefaultRemotingServerChannelHandler(this.loggerFactory);
		return this;
	}
	
	// bind to custom channel
	public RemotingConfiguration bind(ServerChannel channel) {
		channel.setChannelHandler(this.defaultHandler);
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
}
