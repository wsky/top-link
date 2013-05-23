package com.taobao.top.link.channel.embedded;

import java.net.URI;

import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ClientChannel;
import com.taobao.top.link.channel.ClientChannelPooledSelector;

public class EmbeddedClientChannelPooledSelector extends ClientChannelPooledSelector {
	public EmbeddedClientChannelPooledSelector() {
		super();
	}

	public EmbeddedClientChannelPooledSelector(LoggerFactory loggerFactory) {
		super(loggerFactory);
	}

	protected ChannelPool createChannelPool(LoggerFactory loggerFactory, URI uri, int timeout) {
		return new ChannelPool(loggerFactory, uri, timeout);
	}

	public class EmbeddedChannelPool extends ChannelPool {
		public EmbeddedChannelPool(LoggerFactory loggerFactory, URI uri, int timeout) {
			super(loggerFactory, uri, timeout);
		}

		@Override
		public ClientChannel create() throws ChannelException {
			return uri.getScheme().equalsIgnoreCase("ws") ||
					uri.getScheme().equalsIgnoreCase("wss") ?
					EmbeddedWebSocketClient.connect(this.loggerFactory, this.uri, this.timeout) :
					super.create();
		}
	}
}