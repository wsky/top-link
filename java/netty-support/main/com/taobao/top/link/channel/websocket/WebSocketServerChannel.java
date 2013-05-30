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
		// http://netty.io/3.6/xref/org/jboss/netty/channel/socket/nio/DefaultNioSocketChannelConfig.html
		// http://stackoverflow.com/questions/8655973/latency-in-netty-due-to-passing-requests-from-boss-thread-to-worker-thread
		// http://docs.jboss.org/netty/3.2/api/org/jboss/netty/channel/socket/ServerSocketChannelConfig.html
		// http://docs.jboss.org/netty/3.2/api/org/jboss/netty/channel/socket/nio/NioSocketChannelConfig.html
		// http://docs.oracle.com/javase/6/docs/technotes/guides/net/socketOpt.html
		// http://stackoverflow.com/questions/9916796/tuning-netty-on-32-core-10gbit-hosts
		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("backlog", 1024);
		// bootstrap.setOption("writeSpinCount", 16);
		// bootstrap.setOption("writeBufferHighWaterMark", 64 * 1024 * 1024);
		// bootstrap.setOption("writeBufferLowWaterMark", 32 * 1024 * 1024);
		// bootstrap.setOption("receiveBufferSizePredictor", 16);
		// bootstrap.setOption("receiveBufferSizePredictorFactory", 16);
		bootstrap.setOption("sendBufferSize", 1048576);
		bootstrap.setOption("receiveBufferSize", 1048576);
		bootstrap.setOption("child.sendBufferSize", 1048576);
		bootstrap.setOption("child.receiveBufferSize", 1048576);
		bootstrap.setOption("child.tcpNoDelay", true);
		this.prepareBootstrap(this.bootstrap);

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
