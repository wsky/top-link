package com.taobao.top.link.channel.tcp;

import java.net.URI;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;

import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ClientChannel;
import com.taobao.top.link.channel.ConnectingChannelHandler;
import com.taobao.top.link.channel.netty.NettyClient;

public class TcpClient extends NettyClient {
	public static ClientChannel connect(LoggerFactory loggerFactory, URI uri, int connectTimeoutMillis)
			throws ChannelException {
		Logger logger = loggerFactory.create(String.format("TcpClientHandler-%s", uri));

		TcpClientChannel clientChannel = new TcpClientChannel();
		clientChannel.setUri(uri);

		ConnectingChannelHandler handler = new ConnectingChannelHandler();
		clientChannel.setChannelHandler(handler);

		TcpClientUpstreamHandler tcpHandler = new TcpClientUpstreamHandler(logger, clientChannel);
		ChannelPipeline pipeline = Channels.pipeline();
		// connect
		prepareAndConnect(logger, uri,
				pipeline, tcpHandler,
				uri.getScheme().equalsIgnoreCase("wss"),
				connectTimeoutMillis);
		return clientChannel;
	}
}
