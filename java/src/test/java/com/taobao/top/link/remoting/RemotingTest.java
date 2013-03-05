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
	public void call_timing_test() throws URISyntaxException, ChannelException, InterruptedException {
		final URI uri = new URI("ws://localhost:9002/link");
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(uri.getHost(), uri.getPort());
		Endpoint server = new Endpoint();
		server.setChannelHandler(new RemotingServerChannelHandler() {
			@Override
			public void onRequest(ByteBuffer requestBuffer, ByteBuffer responseBuffer) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				responseBuffer.put(new byte[] { requestBuffer.get(), requestBuffer.get(), requestBuffer.get(), requestBuffer.get() });
			}
		});
		server.bind(serverChannel);

		byte[] data = new byte[4];
		DynamicProxy proxy = RemotingService.connect(uri);
		for (int i = 0; i < 10; i++) {
			ByteBuffer.wrap(data).putInt(i);
			ByteBuffer resultBuffer = proxy.send(data, 0, 4);
			assertEquals(i, resultBuffer.getInt());
		}

		// proxy1/2 will share same channel, so rpc flag must be split
		final DynamicProxy proxy1 = RemotingService.connect(uri);
		final DynamicProxy proxy2 = RemotingService.connect(uri);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					byte[] data = new byte[4];
					for (int i = 0; i < 100; i++) {
						System.out.println("thread1");
						ByteBuffer.wrap(data).putInt(i);
						ByteBuffer resultBuffer = proxy1.send(data, 0, 4);
						assertEquals(i, resultBuffer.getInt());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				synchronized (uri) {
					uri.notify();
				}
			}
		}).start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					byte[] data = new byte[4];
					for (int i = 100; i < 200; i++) {
						System.out.println("thread2");
						ByteBuffer.wrap(data).putInt(i);
						ByteBuffer resultBuffer = proxy2.send(data, 0, 4);
						assertEquals(i, resultBuffer.getInt());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				synchronized (uri) {
					uri.notify();
				}
			}
		}).start();

		synchronized (uri) {
			uri.wait();
		}
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

	// @Test(expected = ChannelException.class)
	public void dynamicProxy_test() throws Exception {
		DefaultRemotingServerChannelHandler remotingServerChannelHandler = new DefaultRemotingServerChannelHandler();
		remotingServerChannelHandler.addService(new SampleService());

		URI uri = new URI("ws://localhost:9005/link");
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(uri.getHost(), uri.getPort());
		final Endpoint server = new Endpoint();
		server.setChannelHandler(remotingServerChannelHandler);
		server.bind(serverChannel);

		SampleServiceInterface sampleService = (SampleServiceInterface) RemotingService.connect(uri, SampleServiceInterface.class);
		assertEquals("hi", sampleService.echo("hi"));
	}
}
