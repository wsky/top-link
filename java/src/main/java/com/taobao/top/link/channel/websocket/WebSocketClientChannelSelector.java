package com.taobao.top.link.channel.websocket;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Hashtable;
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

import com.taobao.top.link.Identity;
import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ChannelContext;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ChannelHandler;
import com.taobao.top.link.channel.ClientChannel;
import com.taobao.top.link.channel.ClientChannelSelector;

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
		if (channels.get(url) == null ||
				!channels.get(url).isConnected()) {
			synchronized (this.lockObject) {
				if (channels.get(url) == null ||
						!channels.get(url).isConnected()) {
					channels.put(url, this.connect(uri, identity, CONNECT_TIMEOUT));
				}
			}
		}
		return channels.get(url);
	}

	@Override
	public void returnChannel(ClientChannel channel) {
		// shared channel
	}

	public ClientChannel connect(URI uri, Identity identity, int timeout)
			throws ChannelException {
		Logger logger = this.loggerFactory.create(String.format("WebSocketClientHandler-%s", uri));

		WebSocketClientChannel clientChannel = new WebSocketClientChannel();
		clientChannel.setUri(uri);

		ConnectingChannelHandler handler = new ConnectingChannelHandler();
		clientChannel.setChannelHandler(handler);

		WebSocketClientUpstreamHandler wsHandler = new WebSocketClientUpstreamHandler(logger, clientChannel);
		ClientBootstrap bootstrap = this.prepareBootstrap(logger, wsHandler);
		// connect
		ChannelFuture future=null;
		try {
			future = bootstrap.connect(new InetSocketAddress(uri.getHost(), uri.getPort())).sync();
		} catch (Exception e) {
			throw new ChannelException("connect error", e);
		}
		Channel channel = future.getChannel();
		// handshake
		try {
			WebSocketClientHandshaker handshaker = this.wsFactory.
					newHandshaker(uri, WebSocketVersion.V13, null, true, null);
			wsHandler.handshaker = handshaker;
			handshaker.handshake(channel);
			synchronized (handler.syncObject) {
				handler.syncObject.wait(timeout);
			}
		} catch (Exception e) {
			throw new ChannelException("handshake error", e);
		}

		if (wsHandler.handshaker.isHandshakeComplete())
			return clientChannel;
		if (handler.error != null)
			throw new ChannelException("connect fail: " + handler.error.getMessage(), handler.error);

		throw new ChannelException("connect timeout");
	}

	private ClientBootstrap prepareBootstrap(Logger logger, WebSocketClientUpstreamHandler wsHandler) {
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
		final ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("decoder", new HttpResponseDecoder());
		pipeline.addLast("encoder", new HttpRequestEncoder());
		pipeline.addLast("handler", wsHandler);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				return pipeline;
			}
		});
		return bootstrap;
	}
	
	class ConnectingChannelHandler implements ChannelHandler {
		public Throwable error;
		public Object syncObject = new Object();

		@Override
		public void onConnect(ChannelContext context) {
			synchronized (syncObject) {
				syncObject.notify();
			}
		}

		@Override
		public void onMessage(ChannelContext context) {
		}

		@Override
		public void onError(ChannelContext context) {
			error = context.getError();
			synchronized (syncObject) {
				syncObject.notify();
			}
		}
	}
}
