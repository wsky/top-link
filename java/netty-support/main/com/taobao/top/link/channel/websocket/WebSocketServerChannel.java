package com.taobao.top.link.channel.websocket;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.netty.NettyServerChannel;

public class WebSocketServerChannel extends NettyServerChannel {
	protected boolean cumulative;

	public WebSocketServerChannel(int port) {
		this(port, false);
	}

	public WebSocketServerChannel(int port, boolean cumulative) {
		this(DefaultLoggerFactory.getDefault(), port, cumulative);
	}

	public WebSocketServerChannel(LoggerFactory factory, int port) {
		this(factory, port, false);
	}

	public WebSocketServerChannel(LoggerFactory factory, int port, boolean cumulative) {
		super(factory, port);
		this.allChannels = new DefaultChannelGroup();
		this.cumulative = cumulative;
	}
	
	protected void preparePipeline(ChannelPipeline pipeline) {
		pipeline.addLast("decoder", new HttpRequestDecoder());
		pipeline.addLast("encoder", new HttpResponseEncoder());
		pipeline.addLast("handler", this.createHandler());
	}

	protected void prepareBootstrap(ServerBootstrap bootstrap) {
	}

	protected WebSocketServerUpstreamHandler createHandler() {
		return new WebSocketServerUpstreamHandler(
				this.loggerFactory,
				this.channelHandler,
				this.allChannels,
				this.cumulative);
	}
}
