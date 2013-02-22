package com.taobao.top.link;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.taobao.top.link.handler.ChannelHandler;
import com.taobao.top.link.websocket.WebSocketServerChannel;

public class EndpointTest {
	@Test
	public void request_reply_test() throws InterruptedException, URISyntaxException {
		URI uri = new URI("ws://localhost:8001/");
		final String request = "hello";
		final String reply = "ok";

		// init server channel
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(uri.getHost(), uri.getPort());
		// init endpoint
		Endpoint endpoint = new Endpoint(new TopIdentity());
		endpoint.setChannelHandler(new ChannelHandler() {
			@Override
			public Identity receiveHandshake(byte[] data, int offset, int length) {
				return new TopIdentity();
			}

			@Override
			public void onReceive(byte[] data, int offset, int length, EndpointContext context) {
				String dataString = new String(data, offset, length);
				if (request.equals(dataString)) {
					System.out.println("request:" + dataString);
					synchronized (request) {
						request.notify();
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					context.reply(reply.getBytes(), 0, reply.length());
				}
				if (reply.equals(dataString)) {
					System.out.println("reply:" + dataString);
					synchronized (reply) {
						reply.notify();
					}
				}
			}
		});
		endpoint.bind(serverChannel);

		// sender, get and send
		try {
			EndpointProxy target = endpoint.getEndpoint(uri);
			target.send(request.getBytes(), 0, request.length());
		} catch (ChannelException e) {
			System.out.println(e.getMessage());
		}

		synchronized (request) {
			request.wait(2000);
		}
		synchronized (reply) {
			reply.wait(2000);
		}
	}

	@Test(expected = ChannelException.class)
	public void connect_error_test() throws ChannelException {
		try {
			new Endpoint(new TopIdentity()).getEndpoint(new URI("ws://localhost:8002/"));
		} catch (ChannelException e) {
			System.out.println(e.getMessage());
			throw e;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void identity_error_test() throws URISyntaxException, ChannelException {
		URI uri = new URI("ws://localhost:8003/");
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(uri.getHost(), uri.getPort());
		Endpoint endpoint = new Endpoint(new TopIdentity());
		endpoint.setChannelHandler(new ChannelHandler() {
			@Override
			public Identity receiveHandshake(byte[] data, int offset, int length) {
				return null;
			}

			@Override
			public void onReceive(byte[] data, int offset, int length, EndpointContext context) {
			}
		});
		endpoint.bind(serverChannel);

		endpoint.getEndpoint(uri);
	}

	@Test(expected = ChannelException.class)
	public void maxIdle_reach_test() throws ChannelException, URISyntaxException, InterruptedException {
		URI uri = new URI("ws://localhost:8004/");
		Endpoint endpoint = run(uri.getPort(), 1);

		EndpointProxy target = endpoint.getEndpoint(uri);
		Thread.sleep(2000);

		target.send("hi".getBytes(), 0, 2);
	}

	@Test
	public void send_error_and_reget_test() throws URISyntaxException, InterruptedException, ChannelException {
		URI uri = new URI("ws://localhost:8005/");
		Endpoint endpoint = run(uri.getPort(), 2);

		EndpointProxy target = endpoint.getEndpoint(uri);
		Thread.sleep(3000);

		try {
			target.send("hi".getBytes(), 0, 2);
			assertTrue(false);
		} catch (ChannelException e) {
			System.out.println(e.getMessage());
		}

		target = endpoint.getEndpoint(uri);
		target.send("hi".getBytes(), 0, 2);
		Thread.sleep(1000);
	}

	private Endpoint run(int port, int maxIdle) throws InterruptedException {
		WebSocketServerChannel serverChannel = new WebSocketServerChannel("localhost", port);
		Endpoint endpoint = new Endpoint(new TopIdentity());
		endpoint.setChannelHandler(new ChannelHandler() {
			@Override
			public Identity receiveHandshake(byte[] data, int offset, int length) {
				return new TopIdentity();
			}

			@Override
			public void onReceive(byte[] data, int offset, int length, EndpointContext context) {
				String dataString = new String(data, offset, length);
				System.out.println(dataString);
			}
		});
		serverChannel.setMaxIdleTimeSeconds(maxIdle);
		endpoint.bind(serverChannel);
		Thread.sleep(1000);
		return endpoint;
	}

	public class TopIdentity implements Identity {

		public String AppKey = "top-link";

		@Override
		public byte[] getData() {
			return this.AppKey.getBytes();
		}
	}
}
