package com.taobao.top.link.channel.websocket;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.channel.ChannelException;

public class WebSocketChannelTest {
	@Test
	public void connect_80_test() throws URISyntaxException {
		try {
			WebSocketClient.connect(new DefaultLoggerFactory(), new URI("ws://localhost/"), 10);
		} catch (ChannelException e) {
			assertEquals("connect timeout", e.getMessage());
		}
	}
}
