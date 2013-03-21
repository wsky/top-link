package com.taobao.top.link;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.junit.Test;

import com.taobao.top.link.handler.SimpleChannelHandler;
import com.taobao.top.link.websocket.WebSocketServerChannel;

public class EndpointTest {
	@Test
	public void request_reply_test() throws InterruptedException, URISyntaxException {
		URI uri = new URI("ws://localhost:8001/link");
		final String request = "hello";
		final String reply = "ok";

		// init server channel
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(uri.getPort());
		// init endpoint
		Endpoint endpoint = new Endpoint();
		endpoint.setChannelHandler(new SimpleChannelHandler() {
			@Override
			public void onReceive(ByteBuffer dataBuffer, EndpointContext context) {
				String dataString = new String(dataBuffer.array(), dataBuffer.arrayOffset(), dataBuffer.capacity());
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
					context.reply(ByteBuffer.wrap(reply.getBytes()));
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

		// use direct buffer
		byte[] data = request.getBytes();
		ByteBuffer buffer = ByteBuffer.allocate(1024).put(data);
		buffer.flip();
		buffer.position(0);
		// sender, get and send
		try {
			EndpointProxy target = endpoint.getEndpoint(uri);
			target.send(buffer);
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

		target.send(ByteBuffer.wrap("hi".getBytes()));
	}

	@Test
	public void send_error_and_reget_test() throws URISyntaxException, InterruptedException, ChannelException {
		URI uri = new URI("ws://localhost:8005/link");
		Endpoint endpoint = run(uri.getPort(), 2);
		ChannalHandlerWrapper handlerWrapper = new ChannalHandlerWrapper();
		endpoint.setChannelHandler(handlerWrapper);

		EndpointProxy target = endpoint.getEndpoint(uri);
		Thread.sleep(3000);

		try {
			target.send(ByteBuffer.wrap("hi".getBytes()));
			assertTrue(false);
		} catch (ChannelException e) {
			System.err.println(e.getMessage());
		}

		target = endpoint.getEndpoint(uri);
		target.send(ByteBuffer.wrap("hi".getBytes()));
		handlerWrapper.waitHandler(1000);
		handlerWrapper.assertHandler(1, 0);
	}

	@Test
	public void getConnected_test() {

	}

	@Test
	public void connected_and_close_then_remove_sender_test() {

	}

	private Endpoint run(int port, int maxIdleSecond) throws InterruptedException {
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(port);
		Endpoint endpoint = new Endpoint();
		endpoint.setChannelHandler(new SimpleChannelHandler() {
			@Override
			public void onReceive(ByteBuffer dataBuffer, EndpointContext context) {
				String dataString = new String(dataBuffer.array(), dataBuffer.arrayOffset(), dataBuffer.capacity());
				System.out.println(dataString);
			}
		});
		serverChannel.setMaxIdleTimeSeconds(maxIdleSecond);
		endpoint.bind(serverChannel);
		Thread.sleep(500);
		return endpoint;
	}
}
