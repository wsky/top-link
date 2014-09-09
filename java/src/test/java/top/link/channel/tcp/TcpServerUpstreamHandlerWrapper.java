package top.link.channel.tcp;

import org.jboss.netty.channel.group.ChannelGroup;

import top.link.LoggerFactory;
import top.link.channel.ChannelHandler;
import top.link.channel.tcp.TcpServerUpstreamHandler;

public class TcpServerUpstreamHandlerWrapper extends TcpServerUpstreamHandler {
	public TcpServerUpstreamHandlerWrapper(LoggerFactory loggerFactory, ChannelHandler channelHandler, ChannelGroup channelGroup) {
		super(loggerFactory, channelHandler, channelGroup);
	}
}
