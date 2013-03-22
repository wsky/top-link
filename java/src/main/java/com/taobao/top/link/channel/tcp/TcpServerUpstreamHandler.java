package com.taobao.top.link.channel.tcp;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;

import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ChannelContext;
import com.taobao.top.link.channel.ChannelHandler;
import com.taobao.top.link.channel.ChannelSender;

//one handler per connection
public class TcpServerUpstreamHandler extends SimpleChannelUpstreamHandler {
	private Logger logger;
	private ChannelHandler channelHandler;
	private ChannelGroup allChannels;
	private ChannelSender sender;

	public TcpServerUpstreamHandler(LoggerFactory loggerFactory,
			ChannelHandler channelHandler,
			ChannelGroup channelGroup) {
		this.logger = loggerFactory.create(this);
		this.channelHandler = channelHandler;
		this.allChannels = channelGroup;
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		this.allChannels.add(e.getChannel());
		this.sender = new TcpChannelSender(ctx);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		if(this.channelHandler!=null)
			this.channelHandler.onMessage(this.createContext(e.getMessage()));
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		if (this.channelHandler != null)
			this.channelHandler.onError(this.createContext(e.getCause()));
		e.getChannel().close();
		this.logger.error("exceptionCaught at server", e.getCause());
	}
	
	private ChannelContext createContext(Object message) {
		ChannelContext ctx = new ChannelContext();
		ctx.setSender(this.sender);
		ctx.setMessage(message);
		return ctx;
	}

	private ChannelContext createContext(Throwable error) {
		ChannelContext ctx = new ChannelContext();
		ctx.setSender(this.sender);
		ctx.setError(error);
		return ctx;
	}
}
