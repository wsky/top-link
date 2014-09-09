package top.link.remoting.netty;

import org.jboss.netty.channel.ChannelPipeline;

import top.link.channel.tcp.TcpServerChannel;

public class NettyRemotingTcpServerChannel extends TcpServerChannel {
	public NettyRemotingTcpServerChannel(int port) {
		super(port);
	}
	
	@Override
	protected void prepareCodec(ChannelPipeline pipeline) {
		pipeline.addLast("decoder", new NettyRemotingDecoder());
	}
}
