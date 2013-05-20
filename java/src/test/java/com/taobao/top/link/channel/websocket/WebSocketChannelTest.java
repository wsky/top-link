package com.taobao.top.link.channel.websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.ResetableTimer;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ClientChannel;

public class WebSocketChannelTest {
	private static URI uri;
	private static URI uriSsl;
	private static WebSocketServerChannelWrapper serverChannelWrapper;

	@BeforeClass
	public static void init() throws URISyntaxException {
		uri = new URI("ws://localhost:8888/");
		uriSsl = new URI("wss://localhost:8888/");

		serverChannelWrapper = new WebSocketServerChannelWrapper(uri.getPort());
		serverChannelWrapper.run();
	}

	@AfterClass
	public static void afterClass() {
		serverChannelWrapper.stop();
	}

	@After
	public void after() {
		serverChannelWrapper.setSSLContext(null);
	}

	@Test
	public void connect_80_test() throws URISyntaxException, ChannelException {
		WebSocketClient.parse(new URI("ws://localhost/"));
		WebSocketClient.parse(new URI("ws://localhost"));
	}

	@Test(expected = ChannelException.class)
	public void connect_timeout_test() throws URISyntaxException, ChannelException {
		try {
			WebSocketClient.connect(DefaultLoggerFactory.getDefault(), new URI("ws://10.10.1.200:8889"), 100);
		} catch (ChannelException e) {
			throw e;
		}
	}

	@Test
	public void heartbeat_test() throws ChannelException, InterruptedException {
		heartbeat_test(uri);
	}

	@Test
	public void ssl_test() throws ChannelException, InterruptedException {
		serverChannelWrapper.ssl();
		heartbeat_test(uriSsl);
	}
	
	private void heartbeat_test(URI uri) throws ChannelException, InterruptedException {
		ClientChannel clientChannel = WebSocketClient.connect(DefaultLoggerFactory.getDefault(), uri, 1000);
		serverChannelWrapper.handlerWrapper.latch = new CountDownLatch(3);
		ResetableTimer timer = new ResetableTimer(100);
		clientChannel.setHeartbeatTimer(timer);
		serverChannelWrapper.handlerWrapper.latch.await();
		timer.stop();
	}
}
