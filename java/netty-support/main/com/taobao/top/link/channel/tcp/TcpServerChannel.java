package com.taobao.top.link.channel.tcp;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ServerChannel;

public class TcpServerChannel extends ServerChannel {
	private LoggerFactory loggerFactory;
	private Logger logger;

	private ServerBootstrap bootstrap;
	private ChannelGroup allChannels;
	private int port;

	public TcpServerChannel(int port) {
		this(DefaultLoggerFactory.getDefault(), port);
	}

	public TcpServerChannel(LoggerFactory factory, int port) {
		super(factory, port);
		this.allChannels = new DefaultChannelGroup();
	}

	@Override
	public void run() {
		this.bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
		this.bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				return Channels.pipeline(new TcpServerUpstreamHandler(loggerFactory, channelHandler, allChannels));
			}
		});
		this.bootstrap.setOption("child.tcpNoDelay", true);
		this.allChannels.add(this.bootstrap.bind(new InetSocketAddress(this.port)));
		this.logger.info("server channel bind at %s", this.port);
	}

	@Override
	public void stop() {
		this.allChannels.close().awaitUninterruptibly();
		this.bootstrap.releaseExternalResources();
		this.logger.info("server channel shutdown");
	}

}
