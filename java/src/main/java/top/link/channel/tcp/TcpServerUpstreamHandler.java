package top.link.channel.tcp;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.group.ChannelGroup;

import top.link.channel.ChannelHandler;
import top.link.channel.ChannelSender;
import top.link.channel.netty.NettyServerUpstreamHandler;

public class TcpServerUpstreamHandler extends NettyServerUpstreamHandler {
	public TcpServerUpstreamHandler(
			ChannelHandler channelHandler,
			ChannelGroup channelGroup) {
		super(channelHandler, channelGroup);
	}
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		if (this.channelHandler != null)
			this.channelHandler.onMessage(this.createContext(e.getMessage()));
	}
	
	@Override
	protected ChannelSender createSender(Channel channel) {
		return new TcpServerChannelSender(channel);
	}
}
