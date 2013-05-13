package com.taobao.top.link.channel.embedded;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.ResetableTimer;
import com.taobao.top.link.WebSocketServerUpstreamHandlerWrapper;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ClientChannel;
import com.taobao.top.link.channel.websocket.WebSocketClient;
import com.taobao.top.link.channel.websocket.WebSocketServerChannel;
import com.taobao.top.link.remoting.CustomServerChannelHandler;

public class EmbeddedWebSocketClientTest {
	private static URI uri1;
	private static URI uri2;
	private static WebSocketServerUpstreamHandlerWrapper wrapper;

	@BeforeClass
	public static void init() throws URISyntaxException {
		uri1 = new URI("ws://localhost:9040/");
		uri2 = new URI("ws://localhost:9041/");

		ServerBootstrap bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("reuseAddress", true);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("decoder", new HttpRequestDecoder());
				pipeline.addLast("encoder", new HttpResponseEncoder());
				pipeline.addLast("handler", wrapper = new WebSocketServerUpstreamHandlerWrapper(
						DefaultLoggerFactory.getDefault(),
						null,
						new DefaultChannelGroup(),
						false));
				return pipeline;
			}
		});
		bootstrap.bind(new InetSocketAddress(uri1.getPort()));

		WebSocketServerChannel serverChannel2 = new WebSocketServerChannel(uri2.getPort());
		serverChannel2.setChannelHandler(new CustomServerChannelHandler());
		serverChannel2.run();
	}

	@Test
	public void connect_test() throws ChannelException {
		EmbeddedWebSocketClient.connect(DefaultLoggerFactory.getDefault(), uri1, 1000);
	}

	@Test(expected = ChannelException.class)
	public void connect_error_test() throws ChannelException, URISyntaxException {
		EmbeddedWebSocketClient.connect(DefaultLoggerFactory.getDefault(), new URI("ws://localhost:9042/"), 1000);
	}

	@Test
	public void pass_header_test() throws ChannelException, URISyntaxException {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(CustomServerChannelHandler.ID, "id");
		WebSocketClient.setHeaders(uri2, headers);
		EmbeddedWebSocketClient.connect(DefaultLoggerFactory.getDefault(), uri2, 1000);
	}

	@Test(expected = ChannelException.class)
	public void pass_wrong_header_test() throws ChannelException, URISyntaxException {
		WebSocketClient.setHeaders(uri2, null);
		EmbeddedWebSocketClient.connect(DefaultLoggerFactory.getDefault(), uri2, 1000);
	}

	@Test
	public void heartbeat_test() throws ChannelException, InterruptedException {
		ClientChannel clientChannel = EmbeddedWebSocketClient.connect(DefaultLoggerFactory.getDefault(), uri1, 1000);
		wrapper.latch = new CountDownLatch(3);
		ResetableTimer timer = new ResetableTimer(100);
		clientChannel.setHeartbeatTimer(timer);
		wrapper.latch.await();
		timer.stop();
	}
}
