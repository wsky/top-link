package top.link;

import java.util.concurrent.CountDownLatch;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.group.ChannelGroup;

import top.link.channel.ChannelHandler;
import top.link.channel.websocket.WebSocketServerUpstreamHandler;

public class WebSocketServerUpstreamHandlerWrapper extends WebSocketServerUpstreamHandler {
	public WebSocketServerUpstreamHandlerWrapper(
			ChannelHandler channelHandler,
			ChannelGroup channelGroup,
			boolean cumulative) {
		super(channelHandler, channelGroup, cumulative);
	}

	public CountDownLatch latch;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (latch != null)
			latch.countDown();
		System.out.println(e.getMessage());
		super.messageReceived(ctx, e);
	}
}
