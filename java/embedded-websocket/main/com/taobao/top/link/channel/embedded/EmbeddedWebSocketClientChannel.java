package com.taobao.top.link.channel.embedded;

import java.net.URI;
import java.nio.ByteBuffer;

import com.taobao.top.link.Text;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ChannelHandler;
import com.taobao.top.link.channel.ClientChannel;
import com.taobao.top.link.embedded.websocket.WebSocket;
import com.taobao.top.link.embedded.websocket.exception.WebSocketException;
import com.taobao.top.link.embedded.websocket.frame.rfc6455.FrameRfc6455;

public class EmbeddedWebSocketClientChannel implements ClientChannel {
	private URI uri;
	protected WebSocket socket;
	protected Exception error;
	private ChannelHandler channelHandler;

	public EmbeddedWebSocketClientChannel() {
	}

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
		this.channelHandler = handler;
	}

	@Override
	public boolean isConnected() {
		return socket.isConnected();
	}

	@Override
	public void send(ByteBuffer dataBuffer, SendHandler sendHandler) throws ChannelException {
		this.checkChannel();
		dataBuffer.flip();
		try {
			// create will copy data to it's sendbuffers
			FrameRfc6455 frame = (FrameRfc6455) this.socket.createFrame(dataBuffer);
			frame.mask();
			this.socket.send(frame);
		} catch (WebSocketException e) {
			throw new ChannelException(Text.WS_SEND_ERROR, e);
		} finally {
			// TODO: onSendComplete just do returnbuffer currently, should add
			// callback to do this like netty
			if (sendHandler != null)
				sendHandler.onSendComplete();
		}
	}

	@Override
	public void send(byte[] data, int offset, int length) throws ChannelException {
		this.send(ByteBuffer.wrap(data, offset, length), null);
	}

	private void checkChannel() throws ChannelException {
		if (!this.socket.isConnected())
			throw new ChannelException(Text.WS_CHANNEL_CLOSED);
	}
}
