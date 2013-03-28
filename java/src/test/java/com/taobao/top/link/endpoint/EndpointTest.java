package com.taobao.top.link.endpoint;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.HashMap;

import org.junit.Test;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.websocket.WebSocketServerChannel;
import com.taobao.top.link.endpoint.Endpoint;
import com.taobao.top.link.endpoint.EndpointProxy;

public class EndpointTest {
	private Identity id1 = new DefaultIdentity("test1");
	private Identity id2 = new DefaultIdentity("test2");

	@Test
	public void send_test() {
	}

	@Test
	public void send_and_wait_test() {
	}

	@Test
	public void request_reply_test() throws InterruptedException, URISyntaxException {
		// URI uri = new URI("ws://localhost:8001/link");
		// final String request = "hello";
		// final String reply = "ok";
		//
		// // init server channel
		// WebSocketServerChannel serverChannel = new
		// WebSocketServerChannel(uri.getPort());
		// // init endpoint
		// Endpoint endpoint = new Endpoint(id1);
		// endpoint.setMessageHandler(new MessageHandler() {
		// @Override
		// public void onMessage(EndpointContext context) throws
		// ChannelException {
		// ByteBuffer dataBuffer = (ByteBuffer) context.getMessage();
		// String dataString = new String(dataBuffer.array(),
		// dataBuffer.arrayOffset(), dataBuffer.capacity());
		// if (request.equals(dataString)) {
		// System.out.println("request:" + dataString);
		// synchronized (request) {
		// request.notify();
		// }
		// try {
		// Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// context.reply(ByteBuffer.wrap(reply.getBytes()));
		// }
		// if (reply.equals(dataString)) {
		// System.out.println("reply:" + dataString);
		// synchronized (reply) {
		// reply.notify();
		// }
		// }
		// }
		// });
		// endpoint.bind(serverChannel);
		//
		// // use direct buffer
		// byte[] data = request.getBytes();
		// ByteBuffer buffer = ByteBuffer.allocate(1024).put(data);
		// buffer.flip();
		// buffer.position(0);
		// // sender, get and send
		// try {
		// EndpointProxy target = endpoint.getEndpoint(id1, uri);
		// target.send(buffer);
		// } catch (ChannelException e) {
		// System.err.println(e.getMessage());
		// }
		// // wait, receive request
		// synchronized (request) {
		// request.wait(2000);
		// }
		// // wait, receive reply
		// synchronized (reply) {
		// reply.wait(2000);
		// }
	}

	@Test(expected = LinkException.class)
	public void connect_error_test() throws LinkException {
		try {
			new Endpoint(id1).getEndpoint(id2, new URI("ws://localhost:8002/link"));
		} catch (ChannelException e) {
			System.out.println(e.getMessage());
			throw e;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Test(expected = ChannelException.class)
	public void maxIdle_reach_test() throws URISyntaxException, InterruptedException, LinkException {
		URI uri = new URI("ws://localhost:8004/link");
		Endpoint endpoint = run(uri.getPort(), 1);

		EndpointProxy target = endpoint.getEndpoint(id1, uri);
		Thread.sleep(2000);

		target.send(null);
	}

	@Test
	public void send_error_and_reget_test() throws URISyntaxException, InterruptedException, LinkException {
		URI uri = new URI("ws://localhost:8005/link");
		Endpoint endpoint = run(uri.getPort(), 3);
		MessageHandlerWrapper handlerWrapper = new MessageHandlerWrapper();
		endpoint.setMessageHandler(handlerWrapper);

		EndpointProxy target = endpoint.getEndpoint(id1, uri);
		Thread.sleep(3500);

		try {
			target.send(null);
			assertTrue(false);
		} catch (ChannelException e) {
			System.err.println(e.getMessage());
		}

		target = endpoint.getEndpoint(id1, uri);
		target.send(new HashMap<String, String>());
		handlerWrapper.waitHandler(1000);
		handlerWrapper.assertHandler(1);
	}

	private Endpoint run(int port, int maxIdleSecond) throws InterruptedException {
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(port);
		Endpoint endpoint = new Endpoint(id1);
		endpoint.setMessageHandler(new MessageHandler() {
			@Override
			public void onMessage(EndpointContext context) throws ChannelException {
				System.out.println(context.getMessage());
			}
		});
		serverChannel.setMaxIdleTimeSeconds(maxIdleSecond);
		endpoint.bind(serverChannel);
		Thread.sleep(500);
		return endpoint;
	}
}
