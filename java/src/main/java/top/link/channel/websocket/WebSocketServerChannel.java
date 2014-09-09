package top.link.channel.websocket;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

import top.link.channel.netty.NettyServerChannel;

public class WebSocketServerChannel extends NettyServerChannel {
	protected boolean cumulative;
	
	public WebSocketServerChannel(int port) {
		this(port, false);
	}
	
	public WebSocketServerChannel(int port, boolean cumulative) {
		super(port);
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
				this.channelHandler,
				this.allChannels,
				this.cumulative);
	}
}
