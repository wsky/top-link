package com.taobao.top.link.channel.websocket;

//import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.junit.Test;

import com.taobao.top.link.channel.ChannelException;

public class WebSocketChannelTest {
	@Test
	public void connect_80_test() throws URISyntaxException, ChannelException {
		ClientBootstrap bootstrap = WebSocketClient.prepareBootstrap(null, null);
		WebSocketClient.connect(bootstrap, new URI("ws://localhost/"));
	}
}
