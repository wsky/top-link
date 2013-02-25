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
		URI uri = new URI("ws://localhost:8001/link");
		final String request = "hello";
		final String reply = "ok";

		// init server channel
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(uri.getHost(), uri.getPort());
		// init endpoint
		Endpoint endpoint = new Endpoint();
		endpoint.setChannelHandler(new ChannelHandler() {
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
			System.err.println(e.getMessage());
		}
		// wait, receive request
		synchronized (request) {
			request.wait(2000);
		}
		// wait, receive reply
		synchronized (reply) {
			reply.wait(2000);
		}
	}

	@Test(expected = ChannelException.class)
	public void connect_error_test() throws ChannelException {
		try {
			new Endpoint().getEndpoint(new URI("ws://localhost:8002/link"));
		} catch (ChannelException e) {
			System.out.println(e.getMessage());
			throw e;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Test(expected = ChannelException.class)
	public void maxIdle_reach_test() throws ChannelException, URISyntaxException, InterruptedException {
		URI uri = new URI("ws://localhost:8004/link");
		Endpoint endpoint = run(uri.getPort(), 1);

		EndpointProxy target = endpoint.getEndpoint(uri);
		Thread.sleep(2000);

		target.send("hi".getBytes(), 0, 2);
	}

	@Test
	public void send_error_and_reget_test() throws URISyntaxException, InterruptedException, ChannelException {
		URI uri = new URI("ws://localhost:8005/link");
		Endpoint endpoint = run(uri.getPort(), 3);

		EndpointProxy target = endpoint.getEndpoint(uri);
		Thread.sleep(3500);

		try {
			target.send("hi".getBytes(), 0, 2);
			assertTrue(false);
		} catch (ChannelException e) {
			System.err.println(e.getMessage());
		}

		target = endpoint.getEndpoint(uri);
		target.send("hi".getBytes(), 0, 2);
		Thread.sleep(1000);
	}


	private Endpoint run(int port, int maxIdle) throws InterruptedException {
		WebSocketServerChannel serverChannel = new WebSocketServerChannel("localhost", port);
		Endpoint endpoint = new Endpoint();
		endpoint.setChannelHandler(new ChannelHandler() {
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
}
