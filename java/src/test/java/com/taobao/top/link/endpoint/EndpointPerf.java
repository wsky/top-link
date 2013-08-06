package com.taobao.top.link.endpoint;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;

import com.clarkware.junitperf.LoadTest;
import com.taobao.top.link.LinkException;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.websocket.WebSocketServerChannel;
import com.taobao.top.link.schedule.BatchedScheduler;
import com.taobao.top.link.schedule.Scheduler;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.textui.ResultPrinter;
import junit.textui.TestRunner;

@Ignore
public class EndpointPerf extends TestCase {
	public static void main(String[] args) throws URISyntaxException, LinkException {
		int user = 100, per = 10000;
		int total = user * per;

		EndpointPerf testCase = new EndpointPerf("send_wait_test");
		LoadTest loadTest = new LoadTest(testCase, user, per);
		// TimedTest timedTest = new TimedTest(loadTest, 10000, false);

		long begin = System.currentTimeMillis();
		new TestRunner(new ResultPrinter(System.out) {
			@Override
			public void startTest(Test test) {
			}
		}).doRun(loadTest);
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
	private Map<String, Object> msg;

	public EndpointPerf(String name) throws URISyntaxException, LinkException {
		super(name);

		URI uri = new URI("ws://localhost:8080/");
		Identity serverIdentity = new DefaultIdentity("server");
		msg = new HashMap<String, Object>();
		msg.put("str", "hello1234567890123456789123456789hello1234567890123456789123456789");

		this.prepareServer(uri, serverIdentity);
		this.serverProxy = new Endpoint(new DefaultIdentity("client")).getEndpoint(serverIdentity, uri);
	}

	// have OOM problem
	public void send_test() throws ChannelException {
		this.serverProxy.send(msg);
	}

	// only suitable for server push mode
	public void send_sync_test() throws ChannelException {
		this.serverProxy.sendSync(msg, 200);
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
			public void onMessage(Map<String, Object> message, Identity messageFrom) {
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

		BatchedScheduler<Identity> batchedScheduler = new BatchedScheduler<Identity>() {
			@Override
			protected int getBatchSize(Runnable task) {
				return 100;
			}

			@Override
			protected boolean areInSameBatch(Runnable next, Runnable first) {
				return true;
			}
		};
		batchedScheduler.setThreadPool(new ThreadPoolExecutor(20, 200, 300, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()));
		batchedScheduler.setUserMaxPendingCount(1000);
		batchedScheduler.start();

		this.server.setScheduler(scheduler);
		this.server.setScheduler(batchedScheduler);
		this.server.bind(new WebSocketServerChannel(uri.getPort(), true));
	}
}
