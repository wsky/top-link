package com.taobao.top.link.remoting;

import java.net.URI;

import com.taobao.top.link.ChannelException;
import com.taobao.top.link.ClientChannel;
import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.websocket.WebSocketChannelSelectHandler;

public class RemotingService {
	public static DynamicProxy connect(URI uri) throws ChannelException {
		ClientChannel channel = new WebSocketChannelSelectHandler(new DefaultLoggerFactory()).getClientChannel(uri);
		RemotingClientChannelHandler channelHandler = new RemotingClientChannelHandler();
		channel.setChannelHandler(channelHandler);
		return new DynamicProxy(channel, channelHandler);
	}
}
