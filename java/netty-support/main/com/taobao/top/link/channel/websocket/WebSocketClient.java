package com.taobao.top.link.channel.websocket;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.jboss.netty.handler.ssl.SslHandler;

import com.taobao.top.link.Logger;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.Text;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ClientChannel;
import com.taobao.top.link.channel.ConnectingChannelHandler;
import com.taobao.top.link.channel.X509AlwaysTrustManager;

public class WebSocketClient {
	private static WebSocketClientHandshakerFactory wsFactory = new WebSocketClientHandshakerFactory();
	private static TrustManager[] trustAllCerts = new TrustManager[] { new X509AlwaysTrustManager() };

	public static ClientChannel connect(LoggerFactory loggerFactory, URI uri, int connectTimeoutMillis)
			throws ChannelException {
		Logger logger = loggerFactory.create(String.format("WebSocketClientHandler-%s", uri));

		WebSocketClientChannel clientChannel = new WebSocketClientChannel();
		clientChannel.setUri(uri);

		ConnectingChannelHandler handler = new ConnectingChannelHandler();
		clientChannel.setChannelHandler(handler);

		WebSocketClientUpstreamHandler wsHandler = new WebSocketClientUpstreamHandler(logger, clientChannel);
		// connect
		Channel channel = prepareAndConnect(logger, uri, wsHandler, connectTimeoutMillis);
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
			throw new ChannelException(Text.WS_CONNECT_FAIL
					+ ": " + handler.error.getMessage(), handler.error);

		throw new ChannelException(Text.WS_CONNECT_TIMEOUT);
	}

	protected static InetSocketAddress parse(URI uri) {
		return new InetSocketAddress(uri.getHost(), uri.getPort() > 0 ? uri.getPort() : 80);
	}

	private static Channel prepareAndConnect(Logger logger,
			URI uri,
			WebSocketClientUpstreamHandler wsHandler,
			int connectTimeoutMillis) throws ChannelException {
		SslHandler sslHandler = createSslHandler(uri);
		ClientBootstrap bootstrap = prepareBootstrap(logger, wsHandler, sslHandler, connectTimeoutMillis);
		return doConnect(uri, bootstrap, sslHandler);
	}

	private static Channel doConnect(URI uri, ClientBootstrap bootstrap, SslHandler sslHandler) throws ChannelException {
		try {
			Channel channel = bootstrap.connect(parse(uri)).syncUninterruptibly().getChannel();
			if (sslHandler != null)
				sslHandler.handshake().syncUninterruptibly();
			return channel;
		} catch (Exception e) {
			throw new ChannelException(Text.WS_CONNECT_ERROR, e);
		}
	}

	private static ClientBootstrap prepareBootstrap(Logger logger,
			WebSocketClientUpstreamHandler wsHandler,
			SslHandler sslHandler,
			int connectTimeoutMillis) {
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
				Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool()));

		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("connectTimeoutMillis", connectTimeoutMillis);

		final ChannelPipeline pipeline = Channels.pipeline();

		if (sslHandler != null)
			pipeline.addLast("ssl", sslHandler);

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

	private static SslHandler createSslHandler(URI uri) {
		if (!uri.getScheme().equalsIgnoreCase("wss"))
			return null;
		try {
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, null);
			SSLEngine sslEngine = sslContext.createSSLEngine();
			sslEngine.setUseClientMode(true);
			return new SslHandler(sslEngine);
		} catch (Exception e) {
			return null;
		}
	}
}
