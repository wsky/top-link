package com.taobao.top.link.endpoint;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.channel.websocket.WebSocketServerChannel;
import com.taobao.top.link.endpoint.Endpoint;

public class IdentityTest {
	private static TopIdentity id1;
	private static TopIdentity id2;
	private static Endpoint e1;
	private static URI URI;

	@BeforeClass
	public static void init() throws InterruptedException, URISyntaxException {
		id1 = new TopIdentity("app1");
		id2 = new TopIdentity("app2");
		URI = new URI("ws://localhost:8010/");
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(URI.getPort());
		e1 = new Endpoint(id1);
		e1.setMessageHandler(new MessageHandlerWrapper());
		e1.bind(serverChannel);
	}

	@Test
	public void connect_with_id_test() throws URISyntaxException, LinkException {
		Endpoint e2 = new Endpoint(id2);
		e2.getEndpoint(id1, URI);

		assertEquals(TopIdentity.class, e1.getConnected().next().getIdentity().getClass());
		assertEquals("app2", e1.getConnected().next().getIdentity().toString());
		assertNotNull(e1.getEndpoint(e2.getIdentity()));
	}

	@Test(expected = LinkException.class)
	public void connect_with_wrong_id_test() throws LinkException {
		Endpoint e2 = new Endpoint(new TopIdentity(""));
		try {
			e2.getEndpoint(id1, URI);
		} catch (LinkException e) {
			assertEquals("id error", e.getMessage());
			assertFalse(e2.getEndpoint(id1).hasValidSender());
			throw e;
		}
	}

	@Test(expected = LinkException.class)
	public void connect_self_test() throws LinkException {
		try {
			e1.getEndpoint(id1, URI);
		} catch (LinkException e) {
			assertEquals("target identity can not equal itself", e.getMessage());
			throw e;
		}
	}
}