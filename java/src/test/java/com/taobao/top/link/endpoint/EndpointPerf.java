package com.taobao.top.link.endpoint;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;

import com.clarkware.junitperf.LoadTest;
import com.taobao.top.link.LinkException;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.websocket.WebSocketServerChannel;
import com.taobao.top.link.schedule.Scheduler;

import junit.framework.TestCase;

@Ignore
public class EndpointPerf extends TestCase {
	public static void main(String[] args) throws URISyntaxException, LinkException {
		int user = 100, per = 10000;
		int total = user * per;

		EndpointPerf testCase = new EndpointPerf("send_wait_test");
		LoadTest loadTest = new LoadTest(testCase, user, per);
		// TimedTest timedTest = new TimedTest(loadTest, 10000, false);

		long begin = System.currentTimeMillis();
		junit.textui.TestRunner.run(loadTest);
		long cost = System.currentTimeMillis() - begin;
		System.out.println(String.format(
				"total:%s, cost:%sms, tps:%scall/s, time:%sms",
				total, cost,
				((float) total / (float) cost) * 1000,
				(float) cost / (float) total));

		// testCase.clear();
		System.exit(0);
	}

	private Endpoint server;
	private EndpointProxy serverProxy;
	private Map<String, String> msg;

	public EndpointPerf(String name) throws URISyntaxException, LinkException {
		super(name);

		URI uri = new URI("ws://localhost:8080/");
		Identity serverIdentity = new DefaultIdentity("server");
		msg = new HashMap<String, String>();
		msg.put("str", "hello1234567890123456789123456789hello1234567890123456789123456789");

		this.prepareServer(uri, serverIdentity);
		this.serverProxy = new Endpoint(new DefaultIdentity("client")).getEndpoint(serverIdentity, uri);
	}

	@Test
	public void send_test() throws ChannelException {
		this.serverProxy.send(msg);
	}

	public void send_wait_test() throws LinkException {
		this.serverProxy.sendAndWait(msg, 100);
	}

	public void clear() {
		this.server.unbindAll();
	}

	private void prepareServer(URI uri, Identity serverIdentity) {
		this.server = new Endpoint(serverIdentity);
		this.server.setMessageHandler(new MessageHandler() {
			@Override
			public void onMessage(Map<String, String> message, Identity messageFrom) {
			}

			@Override
			public void onMessage(EndpointContext context) throws Exception {
				context.reply(context.getMessage());
			}
		});
		Scheduler<Identity> scheduler = new Scheduler<Identity>();
		scheduler.setThreadPool(new ThreadPoolExecutor(20, 200, 300, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()));
		scheduler.setUserMaxPendingCount(1000);
		scheduler.start();
		this.server.setScheduler(scheduler);
		this.server.bind(new WebSocketServerChannel(uri.getPort(), true));
	}
}
