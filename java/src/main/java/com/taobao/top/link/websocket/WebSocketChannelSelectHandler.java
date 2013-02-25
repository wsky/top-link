package com.taobao.top.link.websocket;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Hashtable;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;

import com.taobao.top.link.ChannelException;
import com.taobao.top.link.ClientChannel;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.handler.ChannelHandler;
import com.taobao.top.link.handler.ChannelSelectHandler;
import com.taobao.top.link.websocket.WebSocketClientHandler.ClearHandler;

public class WebSocketChannelSelectHandler implements ChannelSelectHandler {
	private LoggerFactory loggerFactory;
	private Hashtable<String, ClientChannel> channels;
	private WebSocketClientHandshakerFactory wsFactory;

	public WebSocketChannelSelectHandler(LoggerFactory factory) {
		this.loggerFactory = factory;
		this.channels = new Hashtable<String, ClientChannel>();
		this.wsFactory = new WebSocketClientHandshakerFactory();
	}

	@Override
	public ClientChannel getClientChannel(URI uri) throws ChannelException {
		if (!uri.getScheme().equalsIgnoreCase("ws")) {
			return null;
		}
		final String url = uri.toString();
		ClientChannel channel = channels.get(url);
		if (channel == null) {
			channels.put(url, channel = this.connect(uri, 5000, new ClearHandler() {
				@Override
				public void clear() {
					channels.remove(url);
				}
			}));
		}
		return channel;
	}

	private ClientChannel connect(URI uri, int timeout, final ClearHandler clearHandler) throws ChannelException {
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
		final WebSocketClientHandler clientHandler = new WebSocketClientHandler(
				this.loggerFactory.create(String.format("WebSocketClientHandler-%s", uri)));
		clientHandler.clearHandler = clearHandler;

		final ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("decoder", new HttpResponseDecoder());
		pipeline.addLast("encoder", new HttpRequestEncoder());
		pipeline.addLast("handler", clientHandler);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				return pipeline;
			}
		});

		// connect
		ChannelFuture future = null;
		try {
			future = bootstrap.connect(new InetSocketAddress(uri.getHost(), uri.getPort())).sync();
		} catch (Exception e) {
			throw new ChannelException("connect error", e);
		}
		final Channel channel = future.getChannel();

		// handshake
		try {
			WebSocketClientHandshaker handshaker = this.wsFactory.newHandshaker(uri, WebSocketVersion.V13, "mqtt", true, null);
			clientHandler.handshaker = handshaker;
			clientHandler.handshakeFuture = handshaker.handshake(channel);
			synchronized (handshaker) {
				handshaker.wait(timeout);
			}
		} catch (Exception e) {
			throw new ChannelException("handshake error", e);
		}
		if (!clientHandler.handshakeFuture.isSuccess()) {
			throw new ChannelException("handshake fail", clientHandler.handshakeFuture.getCause());
		}

		return new ClientChannel() {
			@Override
			protected void setChannelHandler(ChannelHandler handler) {
				clientHandler.channelHandler = handler;
			}

			@Override
			public void send(byte[] data, int offset, int length) throws ChannelException {
				// prevent unknown exception after connected and get channel
				// channel.write is async default
				if (!channel.isConnected()) {
					clearHandler.clear();
					throw new ChannelException("channel closed");
				}
				ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(data, offset, length);
				BinaryWebSocketFrame frame = new BinaryWebSocketFrame(buffer);
				frame.setFinalFragment(true);
				channel.write(frame);
			}
		};
	}
}
