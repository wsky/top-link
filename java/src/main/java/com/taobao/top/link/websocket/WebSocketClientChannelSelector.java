package com.taobao.top.link.websocket;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;

import com.taobao.top.link.ChannelException;
import com.taobao.top.link.ClientChannel;
import com.taobao.top.link.ClientChannelSelector;
import com.taobao.top.link.Identity;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.websocket.WebSocketClientHandler.ClearHandler;

public class WebSocketClientChannelSelector implements ClientChannelSelector {
	private final static int CONNECT_TIMEOUT = 5000;
	private Hashtable<String, ClientChannel> channels;
	private WebSocketClientHandshakerFactory wsFactory;
	protected LoggerFactory loggerFactory;
	protected Object lockObject;

	public WebSocketClientChannelSelector(LoggerFactory factory) {
		this.loggerFactory = factory;
		this.channels = new Hashtable<String, ClientChannel>();
		this.wsFactory = new WebSocketClientHandshakerFactory();
		this.lockObject = new Object();
	}

	@Override
	public ClientChannel getChannel(URI uri, Identity identity) throws ChannelException {
		if (!uri.getScheme().equalsIgnoreCase("ws")) {
			return null;
		}
		final String url = uri.toString();
		if (channels.get(url) == null) {
			synchronized (this.lockObject) {
				if (channels.get(url) == null) {
					channels.put(url, this.connect(uri,
							identity, CONNECT_TIMEOUT, new ClearHandler() {
								@Override
								public void clear() {
									channels.remove(url);
								}
							}));
				}
			}
		}
		return channels.get(url);
	}

	@Override
	public void returnChannel(ClientChannel channel) {
		// shared channel
	}

	public ClientChannel connect(URI uri, Identity identity, int timeout, final ClearHandler clearHandler) throws ChannelException {
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

		// identity render to httpheader
		Map<String, String> headers = new HashMap<String, String>();
		if (identity != null)
			identity.render(headers);

		// handshake
		try {
			WebSocketClientHandshaker handshaker = this.wsFactory.newHandshaker(uri, WebSocketVersion.V13, "mqtt", true, headers);
			clientHandler.handshaker = handshaker;
			handshaker.handshake(channel);
			synchronized (handshaker) {
				handshaker.wait(timeout);
			}
		} catch (Exception e) {
			throw new ChannelException("handshake error", e);
		}
		if (!clientHandler.handshaker.isHandshakeComplete()) {
			if (clientHandler.failure != null) {
				throw new ChannelException("handshake fail: " + clientHandler.failure.getMessage(), clientHandler.failure);
			} else {
				throw new ChannelException("connect timeout");
			}
		}

		ClientChannel clientChannel = new WebSocketClientChannel(channel, clientHandler, clearHandler);
		clientChannel.setUri(uri);
		return clientChannel;
	}
}
