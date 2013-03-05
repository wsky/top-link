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
			public void onRequest(ByteBuffer requestBuffer, ByteBuffer responseBuffer) {
				responseBuffer.put("ok".getBytes());
			}
		});
		server.bind(serverChannel);

		DynamicProxy proxy = RemotingService.connect(uri);
		ByteBuffer resultBuffer = proxy.send("hi".getBytes(), 0, 2);
		assertEquals("ok", new String(new byte[] { resultBuffer.get(), resultBuffer.get() }));
	}

	@Test
	public void channel_reuse_test() throws URISyntaxException, ChannelException {
		URI uri = new URI("ws://localhost:9002/link");
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(uri.getHost(), uri.getPort());
		Endpoint server = new Endpoint();
		server.setChannelHandler(new RemotingServerChannelHandler() {
			@Override
			public void onRequest(ByteBuffer requestBuffer, ByteBuffer responseBuffer) {
			}
		});
		server.bind(serverChannel);

		DynamicProxy proxy1 = RemotingService.connect(uri);
		DynamicProxy proxy2 = RemotingService.connect(uri);
		assertEquals(proxy1.getChannel(), proxy2.getChannel());
	}

	@Test(expected = ChannelException.class)
	public void execute_timeout_test() throws ChannelException, URISyntaxException {
		URI uri = new URI("ws://localhost:9003/link");
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(uri.getHost(), uri.getPort());
		Endpoint server = new Endpoint();
		server.setChannelHandler(new RemotingServerChannelHandler() {
			@Override
			public void onRequest(ByteBuffer requestBuffer, ByteBuffer responseBuffer) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				responseBuffer.put("ok".getBytes());
			}
		});
		server.bind(serverChannel);

		try {
			DynamicProxy proxy = RemotingService.connect(uri);
			proxy.send("hi".getBytes(), 0, 2, 500);
		} catch (ChannelException e) {
			assertEquals("remoting call timeout", e.getMessage());
			throw e;
		}
	}

	@Test(expected = ChannelException.class)
	public void channel_broken_while_calling_test() throws Exception {
		URI uri = new URI("ws://localhost:9004/link");
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(uri.getHost(), uri.getPort());
		final Endpoint server = new Endpoint();
		server.setChannelHandler(new RemotingServerChannelHandler() {
			@Override
			public void onRequest(ByteBuffer requestBuffer, ByteBuffer responseBuffer) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				responseBuffer.put("ok".getBytes());
			}
		});
		server.bind(serverChannel);

		DynamicProxy proxy = RemotingService.connect(uri);

		// make server broken
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				server.unbind();
			}
		}).start();

		try {
			proxy.send("hi".getBytes(), 0, 2);
		} catch (ChannelException e) {
			assertEquals("channel broken with unknown error", e.getMessage());
			throw e;
		}
	}
}
