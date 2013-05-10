package com.taobao.top.link.channel.embedded;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.websocket.WebSocketClient;
import com.taobao.top.link.channel.websocket.WebSocketServerChannel;
import com.taobao.top.link.remoting.CustomServerChannelHandler;

public class EmbeddedWebSocketClientTest {
	private static URI uri1;
	private static URI uri2;

	@BeforeClass
	public static void init() throws URISyntaxException {
		uri1 = new URI("ws://localhost:9040/");
		uri2 = new URI("ws://localhost:9041/");
		new WebSocketServerChannel(uri1.getPort()).run();
		WebSocketServerChannel serverChannel= new WebSocketServerChannel(uri2.getPort());
		serverChannel.setChannelHandler(new CustomServerChannelHandler());
		serverChannel.run();
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
}
