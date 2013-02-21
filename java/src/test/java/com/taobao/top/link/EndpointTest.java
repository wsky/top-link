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

		// init channel
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

		// get and send
		try {
			EndpointProxy target = endpoint.getEndpoint(uri);
			target.send(request.getBytes(), 0, request.length());
			target.send(request.getBytes(), 0, request.length());
		} catch (ChannelException e) {
			e.printStackTrace();
		}

		synchronized (request) {
			request.wait();
		}
		synchronized (reply) {
			reply.wait();
		}
	}

	@Test(expected = ChannelException.class)
	public void connect_error_test() throws ChannelException {
		try {
			new Endpoint(new TopIdentity()).getEndpoint(new URI("ws://localhost:8002/"));
		} catch (ChannelException e) {
			e.printStackTrace();
			throw e;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public class TopIdentity implements Identity {

		public String AppKey;

		@Override
		public byte[] getData() {
			return this.AppKey.getBytes();
		}
	}
}
