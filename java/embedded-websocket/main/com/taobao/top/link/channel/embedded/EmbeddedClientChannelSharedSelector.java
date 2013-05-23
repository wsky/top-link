package com.taobao.top.link.channel.embedded;

import java.net.URI;

import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ClientChannel;
import com.taobao.top.link.channel.ClientChannelSharedSelector;

public class EmbeddedClientChannelSharedSelector extends ClientChannelSharedSelector {
	public EmbeddedClientChannelSharedSelector() {
		super();
	}

	public EmbeddedClientChannelSharedSelector(LoggerFactory loggerFactory) {
		super(loggerFactory);
	}

	protected ClientChannel connect(LoggerFactory loggerFactory, URI uri, int timeout) throws ChannelException {
		return uri.getScheme().equalsIgnoreCase("ws") ||
				uri.getScheme().equalsIgnoreCase("wss") ?
				EmbeddedWebSocketClient.connect(loggerFactory, uri, timeout) :
				super.connect(loggerFactory, uri, timeout);
	}
}
