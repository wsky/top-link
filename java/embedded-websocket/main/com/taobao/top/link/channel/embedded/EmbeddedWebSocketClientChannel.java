package com.taobao.top.link.channel.embedded;

import java.net.SocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;

import com.taobao.top.link.ResetableTimer;
import com.taobao.top.link.Text;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ChannelHandler;
import com.taobao.top.link.channel.ClientChannel;
import com.taobao.top.link.embedded.websocket.WebSocket;
import com.taobao.top.link.embedded.websocket.exception.WebSocketException;
import com.taobao.top.link.embedded.websocket.frame.rfc6455.CloseFrame;
import com.taobao.top.link.embedded.websocket.frame.rfc6455.FrameRfc6455;
import com.taobao.top.link.embedded.websocket.frame.rfc6455.PingFrame;

public class EmbeddedWebSocketClientChannel implements ClientChannel {
	private URI uri;
	protected WebSocket socket;
	protected Exception error;
	private ChannelHandler channelHandler;
	private ResetableTimer heartbeatTimer;

	public EmbeddedWebSocketClientChannel() {
	}

	@Override
	public SocketAddress getLocalAddress() {
		return null;
	}

	@Override
	public SocketAddress getRemoteAddress() {
		return null;
	}

	public ChannelHandler getChannelHandler() {
		this.delayPing();
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
	public void close(String reason) {
		this.stopHeartbeat();
		try {
			CloseFrame frame = new CloseFrame(1000,
					reason != null ? reason : Text.WS_UNKNOWN_ERROR);
			frame.mask();
			this.socket.send(frame);
		} catch (WebSocketException e) {
			// TODO:log error
		}
	}

	@Override
	public void setHeartbeatTimer(ResetableTimer timer) {
		this.stopHeartbeat();
		this.heartbeatTimer = timer;
		this.heartbeatTimer.setTask(new Runnable() {
			@Override
			public void run() {
				if (!isConnected())
					return;
				PingFrame pingFrame = new PingFrame();
				pingFrame.mask();
				try {
					socket.send(pingFrame);
				} catch (WebSocketException e) {
				}
			}
		});
		this.heartbeatTimer.start();
	}

	@Override
	public void send(ByteBuffer dataBuffer, SendHandler sendHandler) throws ChannelException {
		this.checkChannel();
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
				// maybe not success
				sendHandler.onSendComplete(true);
		}
	}

	@Override
	public void send(byte[] data, int offset, int length) throws ChannelException {
		this.send(ByteBuffer.wrap(data, offset, length), null);
	}

	@Override
	public boolean sendSync(ByteBuffer dataBuffer, SendHandler sendHandler, int timeoutMilliseconds) throws ChannelException {
		throw new ChannelException(Text.DO_NOT_SUPPORT);
	}

	private void checkChannel() throws ChannelException {
		if (!this.socket.isConnected()) {
			this.stopHeartbeat();
			throw new ChannelException(Text.CHANNEL_CLOSED);
		}
		this.delayPing();
	}

	private void delayPing() {
		if (this.heartbeatTimer != null)
			this.heartbeatTimer.delay();
	}

	private void stopHeartbeat() {
		if (this.heartbeatTimer != null)
			try {
				this.heartbeatTimer.stop();
				this.heartbeatTimer = null;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
	}
}
