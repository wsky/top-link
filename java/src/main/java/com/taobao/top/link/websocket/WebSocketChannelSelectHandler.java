package com.taobao.top.link.websocket;

import java.net.URI;

import com.taobao.top.link.ChannelException;
import com.taobao.top.link.ClientChannel;
import com.taobao.top.link.handler.ChannelSelectHandler;

public class WebSocketChannelSelectHandler implements ChannelSelectHandler {

	@Override
	public ClientChannel getClientChannel(URI uri) throws ChannelException {
		String scheme = uri.getScheme();

		if (!scheme.equalsIgnoreCase("ws")) {
			return null;
		}

		ClientChannel channel = ChannelHolder.get(uri);
		if (channel != null) {
			return channel;
		}

		// TODO:connect

		return null;
	}

}
