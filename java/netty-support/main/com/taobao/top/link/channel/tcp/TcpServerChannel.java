package com.taobao.top.link.channel.tcp;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.netty.NettyServerChannel;

public class TcpServerChannel extends NettyServerChannel {

	public TcpServerChannel(int port) {
		this(DefaultLoggerFactory.getDefault(), port);
	}

	public TcpServerChannel(LoggerFactory factory, int port) {
		super(factory, port);
	}

	protected void preparePipeline(ChannelPipeline pipeline) {
		pipeline.addLast("handler", this.createHandler());
	}

	protected void prepareBootstrap(ServerBootstrap bootstrap) {
	}

	protected TcpServerUpstreamHandler createHandler() {
		return new TcpServerUpstreamHandler(
				this.loggerFactory,
				this.channelHandler,
				this.allChannels);
	}
}