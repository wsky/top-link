package com.taobao.top.link.channel.websocket;

import java.net.URI;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
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
import com.taobao.top.link.channel.netty.NettyClient;

public class WebSocketClient extends NettyClient {
	private static WebSocketClientHandshakerFactory wsFactory = new WebSocketClientHandshakerFactory();

	public static ClientChannel connect(LoggerFactory loggerFactory, URI uri, int connectTimeoutMillis)
			throws ChannelException {
		Logger logger = loggerFactory.create(String.format("WebSocketClientHandler-%s", uri));

		WebSocketClientChannel clientChannel = new WebSocketClientChannel();
		clientChannel.setUri(uri);

		ConnectingChannelHandler handler = new ConnectingChannelHandler();
		clientChannel.setChannelHandler(handler);

		WebSocketClientUpstreamHandler wsHandler = new WebSocketClientUpstreamHandler(logger, clientChannel);
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("decoder", new HttpResponseDecoder());
		pipeline.addLast("encoder", new HttpRequestEncoder());
		// connect
		Channel channel = prepareAndConnect(logger, uri, 
				pipeline, wsHandler,
				uri.getScheme().equalsIgnoreCase("wss"), 
				connectTimeoutMillis);
		// handshake
		try {
			WebSocketClientHandshaker handshaker = wsFactory.
					newHandshaker(uri, WebSocketVersion.V13, null, true, WebSocketClientHelper.getHeaders(uri));
			wsHandler.handshaker = handshaker;
			handshaker.handshake(channel);
			// return maybe fast than call
			if (!wsHandler.handshaker.isHandshakeComplete() && handler.error == null) {
				synchronized (handler.syncObject) {
					handler.syncObject.wait(connectTimeoutMillis);
				}
			}
		} catch (Exception e) {
			throw new ChannelException(Text.WS_HANDSHAKE_ERROR, e);
		}

		if (wsHandler.handshaker.isHandshakeComplete())
			return clientChannel;
		if (handler.error != null)
			throw new ChannelException(Text.CONNECT_FAIL
					+ ": " + handler.error.getMessage(), handler.error);

		throw new ChannelException(Text.CONNECT_TIMEOUT);
	}
}
