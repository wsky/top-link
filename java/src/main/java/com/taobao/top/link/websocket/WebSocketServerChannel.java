package com.taobao.top.link.websocket;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.ServerChannel;

public class WebSocketServerChannel extends ServerChannel {
	private LoggerFactory loggerFactory;
	private Logger logger;

	private ServerBootstrap bootstrap;
	private ChannelGroup allChannels;
	private String ip;
	private int port;
	private String url;
	private int maxIdleTimeSeconds = 0;

	public WebSocketServerChannel(String ip, int port) {
		this(new DefaultLoggerFactory(), ip, port);
	}

	public WebSocketServerChannel(LoggerFactory factory, String ip, int port) {
		this.loggerFactory = factory;
		this.logger = factory.create(this);
		this.allChannels = new DefaultChannelGroup();

		this.ip = ip;
		this.port = port;
		this.url = String.format("ws://%s:%s/link", this.ip, this.port);
	}

	public String getServerUrl() {
		return this.url;
	}

	public void setMaxIdleTimeSeconds(int value) {
		this.maxIdleTimeSeconds = value;
	}

	@Override
	protected void run() {
		this.bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(
						Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
		// shared timer for idle
		final Timer timer = new HashedWheelTimer();
		this.bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				if (maxIdleTimeSeconds > 0) {
					pipeline.addLast("idleStateHandler",
							new IdleStateHandler(timer, 0, 0, maxIdleTimeSeconds));
					pipeline.addLast("maxIdleHandler",
							new MaxIdleTimeHandler(loggerFactory, maxIdleTimeSeconds));
				}
				pipeline.addLast("decoder", new HttpRequestDecoder());
				pipeline.addLast("encoder", new HttpResponseEncoder());
				pipeline.addLast("handler",
						new WebSocketServerHandler(loggerFactory, url, getChannelHandler(), allChannels));
				return pipeline;
			}
		});
		this.allChannels.add(this.bootstrap.bind(new InetSocketAddress(this.port)));
		this.logger.info("server channel bind at %s", this.port);
	}

	@Override
	protected void stop() {
		this.allChannels.close().awaitUninterruptibly();
		this.bootstrap.releaseExternalResources();
		this.logger.info("server channel shutdown");
	}
}
