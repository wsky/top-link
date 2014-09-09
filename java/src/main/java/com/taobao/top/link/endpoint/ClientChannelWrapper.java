package com.taobao.top.link.endpoint;

import java.net.URI;

import com.taobao.top.link.ResetableTimer;
import com.taobao.top.link.channel.ChannelHandler;
import com.taobao.top.link.channel.ClientChannel;

public class ClientChannelWrapper extends ChannelSenderWrapper implements ClientChannel {
	private ClientChannel clientChannel;

	public ClientChannelWrapper(ClientChannel clientChannel, int protocolVersion) {
		super(clientChannel, protocolVersion);
		this.clientChannel = clientChannel;
	}

	public boolean isConnected() {
		return this.clientChannel.isConnected();
	}

	public ChannelHandler getChannelHandler() {
		return this.clientChannel.getChannelHandler();
	}

	public void setChannelHandler(ChannelHandler handler) {
		this.clientChannel.setChannelHandler(handler);
	}

	public void setUri(URI uri) {
		this.clientChannel.setUri(uri);
	}

	public URI getUri() {
		return this.clientChannel.getUri();
	}

	public void setHeartbeatTimer(ResetableTimer timer) {
		this.clientChannel.setHeartbeatTimer(timer);
	}
}
