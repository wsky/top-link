package com.taobao.top.link.channel.embedded;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.ResetableTimer;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ClientChannel;
import com.taobao.top.link.channel.websocket.WebSocketClientHelper;
import com.taobao.top.link.channel.websocket.WebSocketServerChannelWrapper;
import com.taobao.top.link.remoting.CustomServerChannelHandler;

public class EmbeddedWebSocketClientTest {
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

	@Before
	public void before() {
		serverChannelWrapper.setSSLContext(null);
		serverChannelWrapper.setChannelHandler(null);
	}

	@Test
	public void connect_test() throws ChannelException {
		EmbeddedWebSocketClient.connect(DefaultLoggerFactory.getDefault(), uri, 1000);
	}

	@Test(expected = ChannelException.class)
	public void connect_error_test() throws ChannelException, URISyntaxException {
		EmbeddedWebSocketClient.connect(DefaultLoggerFactory.getDefault(), new URI("ws://localhost:9042/"), 1000);
	}

	@Test
	public void pass_header_test() throws ChannelException, URISyntaxException {
		serverChannelWrapper.setChannelHandler(new CustomServerChannelHandler());
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(CustomServerChannelHandler.ID, "id");
		WebSocketClientHelper.setHeaders(uri, headers);
		EmbeddedWebSocketClient.connect(DefaultLoggerFactory.getDefault(), uri, 1000);
	}

	@Test(expected = ChannelException.class)
	public void pass_wrong_header_test() throws ChannelException, URISyntaxException {
		serverChannelWrapper.setChannelHandler(new CustomServerChannelHandler());
		WebSocketClientHelper.setHeaders(uri, null);
		EmbeddedWebSocketClient.connect(DefaultLoggerFactory.getDefault(), uri, 1000);
	}

	@Test
	public void heartbeat_test() throws ChannelException, InterruptedException {
		heartbeat_test(uri);
	}

	@Test
	public void ssl_test() throws ChannelException, InterruptedException {
		serverChannelWrapper.ssl();
		EmbeddedWebSocketClient.connect(DefaultLoggerFactory.getDefault(), uriSsl, 1000);
		// heartbeat_test(uriSsl);
	}
	
	@Test
	public void close_test() throws ChannelException, InterruptedException {
		ClientChannel clientChannel = EmbeddedWebSocketClient.connect(DefaultLoggerFactory.getDefault(), uri, 1000);
		assertTrue(clientChannel.isConnected());
		clientChannel.close("close");
		//websocket-client will not known that soon
		assertTrue(clientChannel.isConnected());
		Thread.sleep(100);
		assertFalse(clientChannel.isConnected());
	}

	@Test
	public void server_close_test() throws ChannelException, InterruptedException {
		ClientChannel clientChannel = EmbeddedWebSocketClient.connect(DefaultLoggerFactory.getDefault(), uri, 1000);
		assertTrue(clientChannel.isConnected());
		serverChannelWrapper.stop();
		Thread.sleep(100);
		assertFalse(clientChannel.isConnected());

	}

	private void heartbeat_test(URI uri) throws ChannelException, InterruptedException {
		ClientChannel clientChannel = EmbeddedWebSocketClient.connect(DefaultLoggerFactory.getDefault(), uri, 1000);
		serverChannelWrapper.handlerWrapper.latch = new CountDownLatch(3);
		ResetableTimer timer = new ResetableTimer(100);
		clientChannel.setHeartbeatTimer(timer);
		serverChannelWrapper.handlerWrapper.latch.await();
		timer.stop();

	}
}
