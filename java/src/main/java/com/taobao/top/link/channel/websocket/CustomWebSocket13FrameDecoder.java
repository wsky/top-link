package com.taobao.top.link.channel.websocket;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.websocketx.WebSocket13FrameDecoder;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;

// improve io-worker thread usage
// https://github.com/wsky/top-link/issues/37
public class CustomWebSocket13FrameDecoder extends WebSocket13FrameDecoder {

	private List<Object> cumulation;

	public CustomWebSocket13FrameDecoder(boolean maskedPayload, boolean allowExtensions) {
		super(maskedPayload, allowExtensions);
	}

	public CustomWebSocket13FrameDecoder(boolean maskedPayload, boolean allowExtensions, long maxFramePayloadLength) {
		super(maskedPayload, allowExtensions, maxFramePayloadLength);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		this.cumulation = new ArrayList<Object>();
		super.messageReceived(ctx, e);
		// batch FireMessageReceived
		if (this.cumulation.size() > 0)
			Channels.fireMessageReceived(ctx, cumulation, e.getRemoteAddress());
	}

	protected final void unfoldAndFireMessageReceived(
			ChannelHandlerContext context, SocketAddress remoteAddress, WebSocketFrame result) {
		this.cumulation.add(result);
	}

	protected final void unfoldAndFireMessageReceived(
			ChannelHandlerContext context, SocketAddress remoteAddress, Object[] result) {
		for (Object r : result)
			this.cumulation.add(r);
	}

	protected final void unfoldAndFireMessageReceived(
			ChannelHandlerContext context, SocketAddress remoteAddress, Iterable<?> result) {
		for (Object r : result)
			this.cumulation.add(r);
	}
}
