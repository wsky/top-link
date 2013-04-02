package com.taobao.top.link.endpoint;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.websocket.WebSocketServerChannel;
import com.taobao.top.link.endpoint.Endpoint;
import com.taobao.top.link.endpoint.EndpointProxy;

public class EndpointTest {
	private static Identity id1 = new DefaultIdentity("test1");
	private static Identity id2 = new DefaultIdentity("test2");

	private static Endpoint e1;
	private static URI URI;
	private static MessageHandlerWrapper handlerWrapper;

	@BeforeClass
	public static void init() throws InterruptedException, URISyntaxException {
		URI = new URI("ws://localhost:8001/link");
		e1 = run(id1, URI.getPort(), 30, handlerWrapper = new MessageHandlerWrapper());
		handlerWrapper.doReply = true;
	}

	@Before
	public void clear() {
		handlerWrapper.clear();
	}

	@Test
	public void connect_test() throws LinkException, URISyntaxException, InterruptedException {
		Endpoint e2 = new Endpoint(id2);
		// connect
		e2.getEndpoint(id1, URI);
		// then they know each other
		assertNotNull(e1.getEndpoint(id2));
		assertNotNull(e2.getEndpoint(id1));
	}

	@Test
	public void send_test() throws LinkException, InterruptedException {
		Endpoint e2 = new Endpoint(id2);
		MessageHandlerWrapper handlerWrapper2 = new MessageHandlerWrapper();
		e2.setMessageHandler(handlerWrapper2);

		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put("key1", "abc中文");
		msg.put("key2", "abcefg");

		e2.getEndpoint(id1, URI).send(msg);

		// target
		handlerWrapper.waitHandler(2000);
		handlerWrapper.assertHandler(1);
		assertEquals(msg.get("key1"), handlerWrapper.lastMessage.get("key1"));
		assertEquals(msg.get("key2"), handlerWrapper.lastMessage.get("key2"));
		// e2
		handlerWrapper2.waitHandler(2000);
		handlerWrapper2.assertHandler(1);
		assertEquals(msg.get("key1"), handlerWrapper2.lastMessage.get("key1"));
		assertEquals(msg.get("key2"), handlerWrapper2.lastMessage.get("key2"));
	}

	@Test
	public void send_and_wait_test() throws LinkException {
		Endpoint e2 = new Endpoint(id2);
		MessageHandlerWrapper handlerWrapper2 = new MessageHandlerWrapper();
		e2.setMessageHandler(handlerWrapper2);

		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put("key1", "abc中文");
		msg.put("key2", "abcefg");

		HashMap<String, String> reMsg = e2.getEndpoint(id1, URI).sendAndWait(msg);
		assertEquals(msg.get("key1"), reMsg.get("key1"));
		assertEquals(msg.get("key2"), reMsg.get("key2"));

		handlerWrapper.assertHandler(1);
		// callback will handle the reply message
		handlerWrapper2.assertHandler(0);
	}

	@Test(expected = LinkException.class)
	public void send_and_wait_error_test() throws LinkException {
		handlerWrapper.doError = true;
		Endpoint e2 = new Endpoint(id2);
		try {
			e2.getEndpoint(id1, URI).sendAndWait(null);
		} catch (LinkException e) {
			e.printStackTrace();
			assertEquals("process error", e.getMessage());
			throw e;
		}
	}

	@Test(expected = LinkException.class)
	public void send_and_wait_timeout_test() throws LinkException, InterruptedException, URISyntaxException {
		URI uri = new URI("ws://localhost:8002/link");
		run(id1, uri.getPort(), 30, new MessageHandler() {
			@Override
			public void onMessage(EndpointContext context) throws Exception {
				Thread.sleep(5000);
			}
		});
		try {
			new Endpoint(id2).getEndpoint(id1, uri).sendAndWait(null, 2);
		} catch (LinkException e) {
			assertEquals("execution timeout", e.getMessage());
			throw e;
		}
	}

	@Test(expected = LinkException.class)
	public void connect_error_test() throws LinkException, URISyntaxException {
		try {
			new Endpoint(id1).getEndpoint(id2, new URI("ws://localhost:8003/link"));
		} catch (ChannelException e) {
			System.out.println(e.getMessage());
			throw e;
		}
	}

	@Test(expected = LinkException.class)
	public void maxIdle_reach_test() throws URISyntaxException, InterruptedException, LinkException {
		URI uri = new URI("ws://localhost:8004/link");
		run(id1, uri.getPort(), 1, null);

		EndpointProxy target = null;
		try {
			target = new Endpoint(id2).getEndpoint(id1, uri);
		} catch (LinkException e) {
			e.printStackTrace();
			assertTrue(false);
		}
		Thread.sleep(2000);

		try {
			target.send(null);
		} catch (LinkException e) {
			e.printStackTrace();
			throw e;
		}

	}

	private static Endpoint run(Identity id, int port, int maxIdleSecond, MessageHandler handler) throws InterruptedException {
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(port);
		Endpoint endpoint = new Endpoint(id);
		endpoint.setMessageHandler(handler == null ? new MessageHandlerWrapper() : handler);
		serverChannel.setMaxIdleTimeSeconds(maxIdleSecond);
		endpoint.bind(serverChannel);
		return endpoint;
	}
}
