package com.taobao.top.link.remoting;

import com.taobao.top.link.Endpoint;
import com.taobao.top.link.ServerChannel;
import com.taobao.top.link.websocket.WebSocketServerChannel;

// combined client/server remoting config helper
public class RemotingConfiguration {
	private static RemotingConfiguration configuration;

	public synchronized static RemotingConfiguration configure() {
		if (configuration == null)
			configuration = new RemotingConfiguration();
		return configuration;
	}

	private Endpoint endpoint;
	private DefaultRemotingServerChannelHandler defaultHandler;

	public RemotingConfiguration() {
		this.defaultHandler = new DefaultRemotingServerChannelHandler();
		this.endpoint = new Endpoint();
		this.endpoint.setChannelHandler(this.defaultHandler);
	}

	// bind to custom channel
	public RemotingConfiguration bind(ServerChannel channel) {
		this.endpoint.bind(channel);
		return this;
	}

	public RemotingConfiguration websocket(int port) {
		this.endpoint.bind(new WebSocketServerChannel(port));
		return this;
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
}
