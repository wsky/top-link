package com.taobao.top.link.endpoint;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.LinkException;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.ClientChannelSharedSelector;
import com.taobao.top.link.channel.websocket.WebSocketClientHelper;
import com.taobao.top.link.channel.websocket.WebSocketServerChannel;
import com.taobao.top.link.remoting.CustomServerChannelHandler;
import com.taobao.top.link.schedule.Scheduler;

public class ExtensionTest {
	private static Identity id1 = new DefaultIdentity("test1");
	private static Identity id2 = new DefaultIdentity("test2");
	private static Endpoint e1;
	private static URI uri;
	private static MessageHandlerWrapper handlerWrapper;

	@BeforeClass
	public static void init() throws InterruptedException, URISyntaxException {
		uri = new URI("ws://localhost:8001/link");

		handlerWrapper = new MessageHandlerWrapper();
		handlerWrapper.doReply = true;

		// enable cumulative
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(uri.getPort(), true);
		e1 = new Endpoint(id1);
		e1.setMessageHandler(handlerWrapper);
		// set custom channelhandler
		e1.setChannelHandler(new CustomEndpointChannelHandler());
		e1.bind(serverChannel);
		// set scheduler
		Scheduler<Identity> scheduler = new Scheduler<Identity>(new DefaultLoggerFactory(true, true, true, true, true));
		scheduler.start();
		e1.setScheduler(scheduler);
	}

	@Before
	public void before() {
		handlerWrapper.clear();

		Map<String, String> headers = new HashMap<String, String>();
		headers.put(CustomServerChannelHandler.ID, "abc");
		WebSocketClientHelper.setHeaders(uri, headers);
	}

	@Test
	public void auth_onConnect_test() throws ChannelException, LinkException, URISyntaxException {
		new Endpoint(id2).getEndpoint(id1, uri).send(new HashMap<String, String>());
	}

	@Test(expected = LinkException.class)
	public void auth_fail_onConnect_test() throws ChannelException, LinkException, URISyntaxException {
		WebSocketClientHelper.setHeaders(uri, null);
		new Endpoint(id2).getEndpoint(id1, uri).send(new HashMap<String, String>());
	}

	@Test
	public void cumulative_test() throws ChannelException, LinkException, InterruptedException {
		Endpoint e2 = new Endpoint(id2);
		EndpointProxy proxy = e2.getEndpoint(id1, uri);
		int total = 100;
		handlerWrapper.latch = new CountDownLatch(total);
		for (int i = 0; i < total; i++)
			proxy.send(new HashMap<String, String>());
		handlerWrapper.latch.await();
		assertEquals(total, handlerWrapper.receive.get());
	}

	@Test
	public void scheduled_endpoint_test() throws ChannelException, LinkException {
		Endpoint e2 = new Endpoint(id2);
		EndpointProxy proxy = e2.getEndpoint(id1, uri);
		proxy.send(new HashMap<String, String>());
		proxy.sendAndWait(new HashMap<String, String>());
	}

	@Test
	public void heartbeat_enable_test() throws LinkException, InterruptedException {
		Endpoint e2 = new Endpoint(id2);
		ClientChannelSharedSelector selector = new ClientChannelSharedSelector();
		selector.setHeartbeat(100);
		e2.setClientChannelSelector(selector);
		e2.getEndpoint(id1, uri);
		Thread.sleep(1000);
	}
}
