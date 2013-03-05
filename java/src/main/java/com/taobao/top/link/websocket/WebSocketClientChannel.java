package com.taobao.top.link.websocket;

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import com.taobao.top.link.ChannelException;
import com.taobao.top.link.ClientChannel;
import com.taobao.top.link.handler.ChannelHandler;
import com.taobao.top.link.websocket.WebSocketClientHandler.ClearHandler;

public class WebSocketClientChannel extends ClientChannel {
	private Channel channel;
	private WebSocketClientHandler clientHandler;
	private ClearHandler clearHandler;

	public WebSocketClientChannel(Channel channel, WebSocketClientHandler clientHandler, ClearHandler clearHandler) {
		this.channel = channel;
		this.clientHandler = clientHandler;
		this.clearHandler=clearHandler;
	}

	@Override
	public void setChannelHandler(ChannelHandler handler) {
		// TODO:maybe course null when concurrent? use volatile?
		clientHandler.channelHandler = handler;
	}

	@Override
	protected void addOnceChannelHandler(ChannelHandler handler) {
		clientHandler.onceHandlers.add(handler);
	}

	@Override
	public boolean isConnected() {
		return channel.isConnected();
	}

	@Override
	public void send(ByteBuffer dataBuffer, SendHandler sendHandler) throws ChannelException {
		// prevent unknown exception after connected and get channel
		// channel.write is async default
		if (!channel.isConnected()) {
			clearHandler.clear();
			throw new ChannelException("channel closed");
		}
		dataBuffer.position(0);
		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(dataBuffer);
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);
		this.send(frame, sendHandler);
	}

	@Override
	public void send(byte[] data, int offset, int length) throws ChannelException {
		if (!channel.isConnected()) {
			clearHandler.clear();
			throw new ChannelException("channel closed");
		}
		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(data, offset, length);
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);
		this.send(frame, null);
	}

	private void send(BinaryWebSocketFrame frame, final SendHandler sendHandler) throws ChannelException {
		frame.setFinalFragment(true);
		channel.write(frame).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture arg0) throws Exception {
				if (sendHandler != null)
					sendHandler.onSendComplete();
			}
		});
	}
}
