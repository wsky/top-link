package com.taobao.top.link.channel.websocket;

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;

import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ChannelSender;

public abstract class WebSocketChannelSender implements ChannelSender {
	protected Channel channel;

	public WebSocketChannelSender(Channel channel) {
		this.channel = channel;
	}

	@Override
	public void send(ByteBuffer dataBuffer, SendHandler sendHandler) throws ChannelException {
		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(dataBuffer);
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);
		this.send(frame, sendHandler);
	}

	@Override
	public void send(byte[] data, int offset, int length) throws ChannelException {
		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(data, offset, length);
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);
		this.send(frame, null);
	}

	@Override
	public void close(String reason) {
		this.channel.write(new CloseWebSocketFrame(1000, reason));
	}

	private void send(WebSocketFrame frame, final SendHandler sendHandler) throws ChannelException {
		frame.setFinalFragment(true);
		if (sendHandler == null)
			this.channel.write(frame);
		else
			this.channel.write(frame).addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture arg0) throws Exception {
					if (sendHandler != null)
						sendHandler.onSendComplete();
				}
			});
	}

}
