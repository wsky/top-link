package com.taobao.top.link.channel.websocket;

import java.net.URI;
import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ChannelHandler;
import com.taobao.top.link.channel.ClientChannel;

public class WebSocketClientChannel implements ClientChannel {
	private URI uri;
	protected Channel channel;
	private ChannelHandler channelHandler;
	
	public ChannelHandler getChannelHandler() {
		return this.channelHandler;
	}

	@Override
	public void setUri(URI uri) {
		this.uri = uri;
	}

	@Override
	public URI getUri() {
		return this.uri;
	}

	@Override
	public void setChannelHandler(ChannelHandler handler) {
		// TODO:maybe course null when concurrent? use volatile?
		this.channelHandler = handler;
	}

	@Override
	public boolean isConnected() {
		return channel.isConnected();
	}

	@Override
	public void send(ByteBuffer dataBuffer, SendHandler sendHandler) throws ChannelException {
		this.checkChannel();
		dataBuffer.position(0);
		ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(dataBuffer);
		BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);
		this.send(frame, sendHandler);
	}

	@Override
	public void send(byte[] data, int offset, int length) throws ChannelException {
		this.checkChannel();
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

	private void checkChannel() throws ChannelException {
		// prevent unknown exception after connected and get channel
		// channel.write is async default
		if (!channel.isConnected())
			throw new ChannelException("channel closed");
	}
}
