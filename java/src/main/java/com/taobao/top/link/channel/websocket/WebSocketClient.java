package com.taobao.top.link.channel.websocket;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
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

import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.Text;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ClientChannel;
import com.taobao.top.link.channel.ConnectingChannelHandler;

public class WebSocketClient {
	private static WebSocketClientHandshakerFactory wsFactory = new WebSocketClientHandshakerFactory();
	private static Map<String, Map<String, String>> headersByUri = new HashMap<String, Map<String, String>>();

	public static void setHeaders(URI uri, Map<String, String> headers) {
		headersByUri.put(uri.toASCIIString(), headers);
	}

	public static Map<String, String> getHeaders(URI uri) {
		return headersByUri.get(uri.toASCIIString());
	}

	public static ClientChannel connect(LoggerFactory loggerFactory, URI uri, int timeout)
			throws ChannelException {
		Logger logger = loggerFactory.create(String.format("WebSocketClientHandler-%s", uri));

		WebSocketClientChannel clientChannel = new WebSocketClientChannel();
		clientChannel.setUri(uri);

		ConnectingChannelHandler handler = new ConnectingChannelHandler();
		clientChannel.setChannelHandler(handler);

		WebSocketClientUpstreamHandler wsHandler = new WebSocketClientUpstreamHandler(logger, clientChannel);
		ClientBootstrap bootstrap = prepareBootstrap(logger, wsHandler);
		// connect
		ChannelFuture future = connect(bootstrap, uri);
		Channel channel = future.getChannel();
		// handshake
		try {
			WebSocketClientHandshaker handshaker = wsFactory.
					newHandshaker(uri, WebSocketVersion.V13, null, true, getHeaders(uri));
			wsHandler.handshaker = handshaker;
			handshaker.handshake(channel);
			synchronized (handler.syncObject) {
				handler.syncObject.wait(timeout);
			}
		} catch (Exception e) {
			throw new ChannelException(Text.WS_HANDSHAKE_ERROR, e);
		}

		if (wsHandler.handshaker.isHandshakeComplete())
			return clientChannel;
		if (handler.error != null)
			throw new ChannelException(Text.WS_CONNECT_FAIL
					+ ": " + handler.error.getMessage(), handler.error);

		throw new ChannelException(Text.WS_CONNECT_TIMEOUT);
	}

	protected static ChannelFuture connect(ClientBootstrap bootstrap, URI uri) throws ChannelException {
		try {
			return bootstrap.connect(parse(uri)).sync();
		} catch (Exception e) {
			throw new ChannelException(Text.WS_CONNECT_ERROR, e);
		}
	}

	protected static InetSocketAddress parse(URI uri) {
		return new InetSocketAddress(uri.getHost(), uri.getPort() > 0 ? uri.getPort() : 80);
	}

	protected static ClientBootstrap prepareBootstrap(Logger logger, WebSocketClientUpstreamHandler wsHandler) {
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("reuseAddress", true);
		final ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("decoder", new HttpResponseDecoder());
		pipeline.addLast("encoder", new HttpRequestEncoder());
		if (wsHandler != null)
			pipeline.addLast("handler", wsHandler);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				return pipeline;
			}
		});
		return bootstrap;
	}
}
