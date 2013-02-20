package com.taobao.top.link;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.taobao.top.link.handler.ChannelHandler;
import com.taobao.top.link.websocket.WebSocketServerChannel;

public class EndpointTest {
	@Test
	public void bind_test() {
		// init channel
		WebSocketServerChannel serverChannel = new WebSocketServerChannel("localhost", 8080);

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
				assertEquals("hello", dataString);
				context.reply("ok".getBytes(), 0, 2);
			}
		});
		endpoint.bind(serverChannel);

		// get and send
		try {
			EndpointProxy target = endpoint.getEndpoint(new URI("ws://localhost:8080"));
			target.send("hello".getBytes(), 0, 5);
		} catch (ChannelException e) {
			e.printStackTrace();
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
