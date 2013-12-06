package com.taobao.top.link.endpoint;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.link.BufferManager;
import com.taobao.top.link.LinkException;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.embedded.EmbeddedClientChannelSharedSelector;
import com.taobao.top.link.channel.websocket.WebSocketServerChannel;

public class EndpointWithEmbeddedClientTest {
	private static Identity id1 = new DefaultIdentity("test1");
	private static Identity id2 = new DefaultIdentity("test2");
	private static URI uri;

	@BeforeClass
	public static void init() throws InterruptedException, URISyntaxException {
		uri = new URI("ws://localhost:9060/");
		Endpoint endpoint = new Endpoint(id1);
		MessageHandlerWrapper handlerWrapper = new MessageHandlerWrapper();
		handlerWrapper.doReply = true;
		// handlerWrapper.print = true;
		endpoint.setMessageHandler(handlerWrapper);
		endpoint.bind(new WebSocketServerChannel(uri.getPort()));
	}

	@Test
	public void send_test() throws ChannelException, LinkException {
		Endpoint e2 = new Endpoint(id2);
		e2.setClientChannelSelector(new EmbeddedClientChannelSharedSelector());
		HashMap<String, Object> msg = new HashMap<String, Object>();
		e2.getEndpoint(id1, uri);
		e2.getEndpoint(id1).send(msg);
		e2.getEndpoint(id1).sendAndWait(msg);
	}

	@Test
	public void send_long_msg_test() throws ChannelException, LinkException {
		BufferManager.setBufferSize(1024 * 1024);
		Endpoint e2 = new Endpoint(id2);
		e2.setClientChannelSelector(new EmbeddedClientChannelSharedSelector());
		e2.getEndpoint(id1, uri);

		HashMap<String, Object> msg = new HashMap<String, Object>();
		String k = "";
		for (int i = 0; i < 128; i++) {
			k += "i";
		}
		msg.put("key", k);
		e2.getEndpoint(id1).sendAndWait(msg);

		for (int i = 0; i < 5; i++) {
			k += k;
		}
		msg.put("key", k);
		e2.getEndpoint(id1).sendAndWait(msg);
	}

	// @Test
	public void perf_simply_test() throws LinkException {
		Endpoint e2 = new Endpoint(id2);
		e2.setClientChannelSelector(new EmbeddedClientChannelSharedSelector());
		EndpointProxy proxy = e2.getEndpoint(id1, uri);
		HashMap<String, Object> msg = new HashMap<String, Object>();
		String k = "";
		for (int i = 0; i < 128; i++) {
			k += "i";
		}
		msg.put("key", k);

		int total = 100000;
		long begin = System.currentTimeMillis();
		for (int i = 0; i < total; i++) {
			proxy.sendAndWait(msg);
		}
		long cost = System.currentTimeMillis() - begin;
		System.out.println(String.format(
				"total:%s, cost:%sms, tps:%scall/s, time:%sms", total, cost,
				((float) total / (float) cost) * 1000,
				(float) cost / (float) total));
		// total:100000, cost:8296ms, tps:12054.002call/s, time:0.08296ms
	}

	@Test
	public void send_parse_test() throws LinkException {
		Endpoint e2 = new Endpoint(id2);
		e2.setClientChannelSelector(new EmbeddedClientChannelSharedSelector());
		HashMap<String, Object> msg = new HashMap<String, Object>();
		msg.put("content1", "{\"k\":\"123\"}");
		msg.put("content2", "{\"k\":\"123\"}");
		msg.put("content3", "{\"k\":\"123\"}");
		msg.put("content4", new Date());
		e2.getEndpoint(id1, uri);
		for (int i = 0; i < 100; i++)
			e2.getEndpoint(id1).sendAndWait(msg);
	}

	@Test
	public void on_error_test() throws LinkException, InterruptedException {
		Endpoint e2 = new Endpoint(id2);
		e2.setClientChannelSelector(new EmbeddedClientChannelSharedSelector());
		e2.setMessageHandler(new MessageHandler() {
			@Override
			public void onAckMessage(EndpointBaseContext context) {
				throw new NullPointerException();
			}

			@Override
			public void onMessage(EndpointContext context) throws Exception {
			}
		});
		EndpointProxy proxy = e2.getEndpoint(id1, uri);
		proxy.send(null);
		Thread.sleep(500);
		assertFalse(proxy.hasValidSender());
	}
}
