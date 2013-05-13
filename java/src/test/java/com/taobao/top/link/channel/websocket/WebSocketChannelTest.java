package com.taobao.top.link.channel.websocket;

//import static org.junit.Assert.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
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

public class WebSocketChannelTest {
	private static URI uri1;
	private static WebSocketServerUpstreamHandlerWrapper wrapper;

	@BeforeClass
	public static void init() throws URISyntaxException {
		uri1 = new URI("ws://localhost:8090/");

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
	}

	@Test
	public void connect_80_test() throws URISyntaxException, ChannelException {
		WebSocketClient.parse(new URI("ws://localhost/"));
		WebSocketClient.parse(new URI("ws://localhost"));
	}

	@Test
	public void heartbeat_test() throws ChannelException, InterruptedException {
		ClientChannel clientChannel = WebSocketClient.connect(DefaultLoggerFactory.getDefault(), uri1, 1000);
		wrapper.latch = new CountDownLatch(3);
		ResetableTimer timer = new ResetableTimer(100);
		clientChannel.setHeartbeatTimer(timer);
		wrapper.latch.await();
		timer.stop();
	}
}
