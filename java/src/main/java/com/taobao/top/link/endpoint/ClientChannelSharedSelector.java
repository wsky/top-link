package com.taobao.top.link.endpoint;

import java.net.URI;
import java.util.Hashtable;

import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ClientChannel;
import com.taobao.top.link.channel.ClientChannelSelector;
import com.taobao.top.link.channel.websocket.WebSocketClient;

public class ClientChannelSharedSelector implements ClientChannelSelector {
	private final static int CONNECT_TIMEOUT = 5000;
	private Hashtable<String, ClientChannel> channels;
	protected Object lockObject;

	public ClientChannelSharedSelector() {
		this.channels = new Hashtable<String, ClientChannel>();
		this.lockObject = new Object();
	}

	@Override
	public ClientChannel getChannel(URI uri) throws ChannelException {
		if (!uri.getScheme().equalsIgnoreCase("ws")) {
			return null;
		}
		final String url = uri.toString();
		if (channels.get(url) == null ||
				!channels.get(url).isConnected()) {
			synchronized (this.lockObject) {
				if (channels.get(url) == null ||
						!channels.get(url).isConnected()) {
					channels.put(url,
							WebSocketClient.connect(uri, CONNECT_TIMEOUT));
				}
			}
		}
		return channels.get(url);
	}

	@Override
	public void returnChannel(ClientChannel channel) {
		// shared channel
	}
}
