package com.taobao.top.link.remoting;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.junit.Test;

import com.taobao.top.link.ChannelException;
import com.taobao.top.link.Endpoint;
import com.taobao.top.link.websocket.WebSocketServerChannel;

public class RemotingTest {
	@Test
	public void call_test() throws URISyntaxException, ChannelException {
		URI uri = new URI("ws://localhost:9001/link");
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(uri.getHost(), uri.getPort());
		Endpoint server = new Endpoint();
		server.setChannelHandler(new RemotingServerChannelHandler() {
			@Override
			public byte[] onRequest(ByteBuffer buffer) {
				return "ok".getBytes();
			}
		});
		server.bind(serverChannel);

		DynamicProxy proxy = RemotingService.connect(uri);
		ByteBuffer resultBuffer = proxy.call("hi".getBytes(), 0, 2);
		assertEquals("ok", new String(new byte[] { resultBuffer.get(), resultBuffer.get() }));
	}

	@Test(expected = ChannelException.class)
	public void execute_timeout_test() throws ChannelException, URISyntaxException {
		URI uri = new URI("ws://localhost:9002/link");
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(uri.getHost(), uri.getPort());
		Endpoint server = new Endpoint();
		server.setChannelHandler(new RemotingServerChannelHandler() {
			@Override
			public byte[] onRequest(ByteBuffer buffer) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return "ok".getBytes();
			}
		});
		server.bind(serverChannel);

		DynamicProxy proxy = RemotingService.connect(uri);
		proxy.call("hi".getBytes(), 0, 2, 500);
	}

	@Test
	public void channel_broken_while_calling_test() {

	}
}
