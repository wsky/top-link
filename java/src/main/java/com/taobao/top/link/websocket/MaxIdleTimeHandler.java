package com.taobao.top.link.websocket;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.timeout.IdleState;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;

import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;

// IdleStateHandler.
// http://docs.jboss.org/netty/3.2/api/org/jboss/netty/handler/timeout/IdleStateHandler.html
public class MaxIdleTimeHandler extends IdleStateAwareChannelHandler {
	private Logger logger;
	private int maxIdleTimeSeconds;

	public MaxIdleTimeHandler(LoggerFactory loggerFactory, int maxIdleTimeSeconds) {
		this.logger = loggerFactory.create(this);
		this.maxIdleTimeSeconds = maxIdleTimeSeconds;
	}

	@Override
	public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws InterruptedException {
		if (e.getState() == IdleState.ALL_IDLE) {
			this.closeChannel(ctx, 1011, "reach max idle time");
			this.logger.info("reach maxIdleTimeSeconds=%s, close client channel", this.maxIdleTimeSeconds);
		}
	}
	
	private void closeChannel(ChannelHandlerContext ctx, int statusCode, String reason) throws InterruptedException {
		ctx.getChannel().write(new CloseWebSocketFrame(statusCode, reason)).sync();
		ctx.getChannel().close();
	}
}