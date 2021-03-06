package top.link.channel.netty;

import java.io.IOException;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import top.link.Text;
import top.link.channel.ChannelContext;
import top.link.channel.ChannelHandler;
import top.link.channel.ChannelSender;

public abstract class NettyServerUpstreamHandler extends SimpleChannelUpstreamHandler {
	protected Logger logger;
	protected Logger ioErrorLogger;
	protected ChannelHandler channelHandler;
	protected ChannelGroup allChannels;
	protected ChannelSender sender;
	protected String closedReason;
	
	public NettyServerUpstreamHandler(
			ChannelHandler channelHandler,
			ChannelGroup channelGroup) {
		this.logger = LoggerFactory.getLogger(this.getClass());
		this.ioErrorLogger = LoggerFactory.getLogger(this.getClass().getSimpleName() + ".IOError");
		this.channelHandler = channelHandler;
		this.allChannels = channelGroup;
	}
	
	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		this.allChannels.add(e.getChannel());
		this.sender = this.createSender(e.getChannel());
	}
	
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		if (this.closedReason == null)
			this.logger.warn(Text.TCP_CHANNEL_CLOSED);
		if (this.channelHandler != null)
			this.channelHandler.onClosed(this.closedReason);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		Throwable t = e.getCause();
		
		if (this.channelHandler != null)
			this.channelHandler.onError(this.createContext(t));
		
		e.getChannel().close();
		
		if (t instanceof IOException)
			this.ioErrorLogger.error(Text.ERROR_AT_SERVER, t);
		else
			this.logger.error(Text.ERROR_AT_SERVER, t);
	}
	
	protected abstract ChannelSender createSender(Channel channel);
	
	protected ChannelContext createContext(Object message) {
		ChannelContext ctx = new ChannelContext();
		ctx.setSender(this.sender);
		ctx.setMessage(message);
		return ctx;
	}
	
	protected ChannelContext createContext(Throwable error) {
		ChannelContext ctx = new ChannelContext();
		ctx.setSender(this.sender);
		ctx.setError(error);
		return ctx;
	}
}
