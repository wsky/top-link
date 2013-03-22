package com.taobao.top.link.channel.websocket;

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import com.taobao.top.link.channel.ChannelSender.SendHandler;
import com.taobao.top.link.endpoint.EndpointContext;

public class WebSocketEndpointContext extends EndpointContext {
	private ChannelHandlerContext ctx;

	public WebSocketEndpointContext(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void reply(byte[] data, int offset, int length) {
		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(data, offset, length);
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);
		frame.setFinalFragment(true);
		ctx.getChannel().write(frame);
	}

	@Override
	public void reply(ByteBuffer dataBuffer) {
		this.reply(dataBuffer, null);
	}

	@Override
	public void reply(ByteBuffer dataBuffer, final SendHandler sendHandler) {
		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(dataBuffer);
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);
		frame.setFinalFragment(true);
		ctx.getChannel().write(frame).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture arg0) throws Exception {
				if (sendHandler != null)
					sendHandler.onSendComplete();
			}
		});
		;
	}
}
