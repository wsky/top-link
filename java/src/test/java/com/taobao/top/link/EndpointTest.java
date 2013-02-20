package com.taobao.top.link;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.taobao.top.link.handler.ChannelHandler;
import com.taobao.top.link.websocket.WebSocketServerChannel;

public class EndpointTest {
	@Test
	public void bind_test() throws InterruptedException, URISyntaxException {

		URI uri = new URI("ws://localhost:8001/ws");

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
				System.out.println("receive:" + dataString);
				assertEquals("hello", dataString);
				context.reply("ok".getBytes(), 0, 2);
			}
		});
		endpoint.bind(serverChannel);

		Thread.sleep(1000);

		// get and send
		try {
			EndpointProxy target = endpoint.getEndpoint(uri);
			//Thread.sleep(5000);
			target.send("hello".getBytes(), 0, 5);
			//Thread.sleep(2000);
			target.send("hello".getBytes(), 0, 5);
		} catch (ChannelException e) {
			e.printStackTrace();
		}

		Thread.sleep(10000);
	}

	public class TopIdentity implements Identity {

		public String AppKey;

		@Override
		public byte[] getData() {
			return this.AppKey.getBytes();
		}

		@Override
		public URI getUri() {
			try {
				return new URI("ws://localhost:8080");
			} catch (URISyntaxException e) {
				e.printStackTrace();
				return null;
			}
		}

	}
}
