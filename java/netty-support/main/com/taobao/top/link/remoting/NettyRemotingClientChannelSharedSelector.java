package com.taobao.top.link.remoting;

import java.net.URI;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;

import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ClientChannel;
import com.taobao.top.link.channel.ClientChannelSharedSelector;
import com.taobao.top.link.channel.tcp.TcpClient;

public class NettyRemotingClientChannelSharedSelector extends ClientChannelSharedSelector {
	@Override
	protected ClientChannel connect(LoggerFactory loggerFactory, URI uri, int timeout) throws ChannelException {
		if (uri.getScheme().equalsIgnoreCase("tcp") || 
				uri.getScheme().equalsIgnoreCase("ssl")) {
			ChannelPipeline pipeline = Channels.pipeline();
			pipeline.addLast("decoder", new NettyRemotingDecoder());
			return TcpClient.connect(loggerFactory, uri, timeout, pipeline);
		}
		return super.connect(loggerFactory, uri, timeout);
	}
}
