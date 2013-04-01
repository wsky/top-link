package com.taobao.top.link.endpoint;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
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
	public void connect_test() throws LinkException, URISyntaxException, InterruptedException {
		URI uri = new URI("ws://localhost:8001/link");
		Endpoint e1 = this.run(uri.getPort(), 10);
		Endpoint e2 = new Endpoint(id2);
		// connect
		e2.getEndpoint(id1, uri);
		// then they know each other
		assertNotNull(e1.getEndpoint(id2));
		assertNotNull(e2.getEndpoint(id1));
	}

	@Test
	public void send_test() {
		
	}

	@Test
	public void send_and_wait_test() {
	}

	@Test
	public void request_reply_test() throws InterruptedException, URISyntaxException {

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

	@Test(expected = LinkException.class)
	public void maxIdle_reach_test() throws URISyntaxException, InterruptedException, LinkException {
		URI uri = new URI("ws://localhost:8004/link");
		Endpoint endpoint = run(uri.getPort(), 1);

		EndpointProxy target = endpoint.getEndpoint(id1, uri);
		Thread.sleep(2000);

		target.send(null);
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
