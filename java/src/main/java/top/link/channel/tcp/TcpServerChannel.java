package top.link.channel.tcp;

import org.jboss.netty.channel.ChannelPipeline;

import top.link.channel.netty.NettyServerChannel;

public abstract class TcpServerChannel extends NettyServerChannel {
	public TcpServerChannel(int port) {
		super(port);
	}
	
	protected void preparePipeline(ChannelPipeline pipeline) {
		this.prepareCodec(pipeline);
		pipeline.addLast("handler", this.createHandler());
	}
	
	protected abstract void prepareCodec(ChannelPipeline pipeline);
	
	protected TcpServerUpstreamHandler createHandler() {
		return new TcpServerUpstreamHandler(
				this.channelHandler,
				this.allChannels);
	}
}