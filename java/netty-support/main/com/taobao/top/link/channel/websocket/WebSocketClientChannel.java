package com.taobao.top.link.channel.websocket;

import java.net.URI;
import java.nio.ByteBuffer;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;

import com.taobao.top.link.ResetableTimer;
import com.taobao.top.link.Text;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ChannelHandler;
import com.taobao.top.link.channel.ClientChannel;
import com.taobao.top.link.channel.netty.NettyClientChannel;

public class WebSocketClientChannel extends WebSocketChannelSender implements ClientChannel, NettyClientChannel {
	private URI uri;
	private ChannelHandler channelHandler;
	private ResetableTimer heartbeatTimer;

	public WebSocketClientChannel() {
		super(null);
	}

	public ChannelHandler getChannelHandler() {
		this.delayPing();
		return this.channelHandler;
	}

	@Override
	public void setChannel(Channel channel) {
		this.channel = channel;
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
		return this.channel.isConnected();
	}

	@Override
	public void close(String reason) {
		this.stopHeartbeat();
		super.close(reason);
	}

	@Override
	public void setHeartbeatTimer(ResetableTimer timer) {
		this.stopHeartbeat();
		this.heartbeatTimer = timer;
		this.heartbeatTimer.setTask(new Runnable() {
			@Override
			public void run() {
				if (isConnected())
					channel.write(new PingWebSocketFrame());
			}
		});
		this.heartbeatTimer.start();
	}

	@Override
	public void send(ByteBuffer dataBuffer, SendHandler sendHandler) throws ChannelException {
		this.checkChannel();
		super.send(dataBuffer, sendHandler);
	}

	@Override
	public void send(byte[] data, int offset, int length) throws ChannelException {
		this.checkChannel();
		super.send(data, offset, length);
	}

	private void checkChannel() throws ChannelException {
		// prevent unknown exception after connected and get channel
		// channel.write is async default
		if (!this.channel.isConnected()) {
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
