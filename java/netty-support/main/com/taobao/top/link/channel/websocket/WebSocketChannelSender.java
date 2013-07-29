package com.taobao.top.link.channel.websocket;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;

import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.netty.NettyChannelSender;

public abstract class WebSocketChannelSender extends NettyChannelSender {
	public WebSocketChannelSender(Channel channel) {
		super(channel);
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

		// FIXME rewrite to sendSync(int timeout)
		final CountDownLatch latch = new CountDownLatch(1);

		this.channel.write(frame).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture arg0) throws Exception {
				latch.countDown();
			}
		});

		try {
			if (!latch.await(100, TimeUnit.MILLISECONDS))
				throw new ChannelException("flush timeout in 100ms");
		} catch (InterruptedException e) {
			throw new ChannelException(e.getMessage(), e);
		} finally {
			if (sendHandler != null)
				sendHandler.onSendComplete();
		}
	}

}
