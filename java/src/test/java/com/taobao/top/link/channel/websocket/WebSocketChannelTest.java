package com.taobao.top.link.channel.websocket;

//import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.taobao.top.link.channel.ChannelException;

public class WebSocketChannelTest {
	@Test
	public void connect_80_test() throws URISyntaxException, ChannelException {
		WebSocketClient.parse(new URI("ws://localhost/"));
		WebSocketClient.parse(new URI("ws://localhost"));
	}
}
