package top.link.channel.tcp;

import org.jboss.netty.channel.group.ChannelGroup;

import top.link.channel.ChannelHandler;
import top.link.channel.tcp.TcpServerUpstreamHandler;

public class TcpServerUpstreamHandlerWrapper extends TcpServerUpstreamHandler {
	public TcpServerUpstreamHandlerWrapper(ChannelHandler channelHandler, ChannelGroup channelGroup) {
		super(channelHandler, channelGroup);
	}
}
