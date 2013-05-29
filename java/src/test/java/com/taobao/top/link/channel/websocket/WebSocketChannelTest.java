package com.taobao.top.link.channel.websocket;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.ResetableTimer;
import com.taobao.top.link.channel.ChannelContext;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ChannelHandler;
import com.taobao.top.link.channel.ClientChannel;
import com.taobao.top.link.channel.ServerChannelSender;

public class WebSocketChannelTest {
	private static URI uri;
	private static URI uriSsl;
	private static WebSocketServerChannelWrapper serverChannelWrapper;
	private static LoggerFactory loggerFactory = DefaultLoggerFactory.getDefault();
	private static ServerChannelSender connectedSender;

	@BeforeClass
	public static void init() throws URISyntaxException {
		uri = new URI("ws://localhost:8888/");
		uriSsl = new URI("wss://localhost:8888/");

		serverChannelWrapper = new WebSocketServerChannelWrapper(uri.getPort());
		serverChannelWrapper.run();
		serverChannelWrapper.setChannelHandler(new ChannelHandler() {
			@Override
			public void onMessage(ChannelContext context) throws Exception {
			}

			@Override
			public void onError(ChannelContext context) throws Exception {
			}

			@Override
			public void onConnect(ChannelContext context) throws Exception {
				connectedSender = (ServerChannelSender)context.getSender();
			}
		});
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
			WebSocketClient.connect(loggerFactory, new URI("ws://10.10.1.200:8889"), 100);
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

	@Test
	public void client_close_test() throws ChannelException, URISyntaxException, InterruptedException {
		ClientChannel clientChannel = WebSocketClient.connect(loggerFactory, uri, 1000);
		assertTrue(clientChannel.isConnected());

		clientChannel.close("close");
		Thread.sleep(100);
		assertFalse(clientChannel.isConnected());
		// server got closeframe and than disconnected->closed
	}

	@Test
	public void client_unexpected_close_test() throws ChannelException, InterruptedException {
		WebSocketClientChannel clientChannel = (WebSocketClientChannel) WebSocketClient.connect(loggerFactory, uri, 1000);
		assertTrue(clientChannel.isConnected());

		clientChannel.channel.disconnect().syncUninterruptibly();
		// same as
		// clientChannel.channel.close().syncUninterruptibly();
		Thread.sleep(100);
		assertFalse(clientChannel.isConnected());
		// server got disconnected->closed
		// if FIN not received, server wont know about it
	}

	@Test
	public void server_close_test() throws ChannelException, InterruptedException {
		ClientChannel clientChannel = WebSocketClient.connect(loggerFactory, uri, 1000);
		assertTrue(clientChannel.isConnected());
		// server close
		connectedSender.close("close");
		Thread.sleep(100);
		assertFalse(clientChannel.isConnected());
		assertFalse(connectedSender.isOpen());
	}

	@Test
	public void server_unexpected_close_test() throws ChannelException, InterruptedException {
		ClientChannel clientChannel = WebSocketClient.connect(loggerFactory, uri, 1000);
		assertTrue(clientChannel.isConnected());
		// server close
		serverChannelWrapper.stop();
		Thread.sleep(100);
		assertFalse(clientChannel.isConnected());
		assertFalse(connectedSender.isOpen());
	}

	private void heartbeat_test(URI uri) throws ChannelException, InterruptedException {
		ClientChannel clientChannel = WebSocketClient.connect(loggerFactory, uri, 1000);
		serverChannelWrapper.handlerWrapper.latch = new CountDownLatch(3);
		ResetableTimer timer = new ResetableTimer(100);
		clientChannel.setHeartbeatTimer(timer);
		serverChannelWrapper.handlerWrapper.latch.await();
		timer.stop();
	}
}
