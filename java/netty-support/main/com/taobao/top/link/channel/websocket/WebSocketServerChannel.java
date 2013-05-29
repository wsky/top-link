package com.taobao.top.link.channel.websocket;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.Text;
import com.taobao.top.link.channel.ServerChannel;

public class WebSocketServerChannel extends ServerChannel {
	private ServerBootstrap bootstrap;
	protected ChannelGroup allChannels;
	protected boolean cumulative;
	protected SSLContext sslContext;

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

	public void setSSLContext(SSLContext sslContext) {
		this.sslContext = sslContext;
	}

	@Override
	public void run() {
		this.bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("reuseAddress", true);
		// shared timer for idle
		final Timer timer = new HashedWheelTimer();
		this.bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				if (maxIdleTimeSeconds > 0) {
					pipeline.addLast("idleStateHandler", new IdleStateHandler(timer, 0, 0, maxIdleTimeSeconds));
					pipeline.addLast("maxIdleHandler", new MaxIdleTimeHandler(loggerFactory, maxIdleTimeSeconds));
				}
				if (sslContext != null) {
					SSLEngine sslEngine = sslContext.createSSLEngine();
					sslEngine.setUseClientMode(false);
					pipeline.addLast("ssl", new SslHandler(sslEngine));
				}
				pipeline.addLast("decoder", new HttpRequestDecoder());
				pipeline.addLast("encoder", new HttpResponseEncoder());
				pipeline.addLast("handler", createHandler());
				preparePipeline(pipeline);
				return pipeline;
			}
		});
		this.allChannels.add(this.bootstrap.bind(new InetSocketAddress(this.port)));
		this.logger.info(Text.WS_SERVER_RUN, this.port);
	}

	@Override
	public void stop() {
		this.allChannels.close().awaitUninterruptibly();
		this.bootstrap.releaseExternalResources();
		this.logger.info(Text.WS_SERVER_STOP);
	}

	protected void preparePipeline(ChannelPipeline pipeline) {
	}

	protected WebSocketServerUpstreamHandler createHandler() {
		return new WebSocketServerUpstreamHandler(
				this.loggerFactory,
				this.channelHandler,
				this.allChannels,
				this.cumulative);
	}
}