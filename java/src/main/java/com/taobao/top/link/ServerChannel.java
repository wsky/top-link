package com.taobao.top.link;

import com.taobao.top.link.handler.ChannelHandler;

public abstract class ServerChannel {
	protected Endpoint endpoint;

	protected ChannelHandler getChannelHandler() {
		return this.endpoint.getChannelHandler();
	}

	protected void run(Endpoint endpoint) {
		this.endpoint = endpoint;
		this.run();
	}

	protected abstract void run();
}
