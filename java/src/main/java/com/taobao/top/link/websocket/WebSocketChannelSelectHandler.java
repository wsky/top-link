package com.taobao.top.link.websocket;

import java.net.URI;

import com.taobao.top.link.ClientChannel;
import com.taobao.top.link.handler.ChannelSelectHandler;

public class WebSocketChannelSelectHandler implements ChannelSelectHandler {

	@Override
	public ClientChannel getClientChannel(URI uri) {
		String scheme = uri.getScheme();
		
		if(scheme.equalsIgnoreCase("ws")) 
			return ChannelHolder.get(uri);
		
		return null;
	}

}
