package com.taobao.top.link.remoting;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Ignore;

import junit.framework.Test;
import junit.framework.TestCase;

import com.clarkware.junitperf.LoadTest;
import com.clarkware.junitperf.TestMethodFactory;
import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.channel.ClientChannelSharedSelector;
import com.taobao.top.link.channel.ServerChannel;
import com.taobao.top.link.channel.websocket.WebSocketServerChannel;

@Ignore
public class RemotingPerf extends TestCase {
	private static URI uri;

	public static void main(String[] args) throws URISyntaxException {
		uri = new URI("ws://localhost:8080/");
		prepareServer(uri);

		int user = 100, per = 10000;
		int total = user * per;

		Test testCase = new TestMethodFactory(RemotingPerf.class, "invoke_test");
		LoadTest loadTest = new LoadTest(testCase, user, per);
		// TimedTest timedTest = new TimedTest(loadTest, 10000, false);
		long begin = System.currentTimeMillis();
		junit.textui.TestRunner.run(loadTest);
		long cost = System.currentTimeMillis() - begin;
		System.out.println(String.format(
				"total:%s, cost:%sms, tps:%scall/s, time:%sms", total, cost,
				((float) total / (float) cost) * 1000, (float) cost
						/ (float) total));

		System.exit(0);
	}

	private static void prepareServer(URI uri) {
		RemotingServerChannelHandler handler = new RemotingServerChannelHandler() {
			@Override
			public MethodReturn onMethodCall(MethodCall methodCall, MethodCallContext callContext) {
				MethodReturn methodReturn = new MethodReturn();
				methodReturn.ReturnValue = methodCall.Args[0];
				return methodReturn;
			}
		};
		handler.setThreadPool(new ThreadPoolExecutor(20, 200, 300, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()));
		ServerChannel serverChannel = new WebSocketServerChannel(uri.getPort(), true);
		serverChannel.setChannelHandler(handler);
		serverChannel.run();
	}

	private DynamicProxy proxy;
	private MethodCall call;

	public RemotingPerf(String name) throws URISyntaxException {
		super(name);

		call = new MethodCall();
		call.Args = new Object[] { "hello1234567890123456789123456789hello1234567890123456789123456789" };

		proxy = new DynamicProxy(uri,
				new ClientChannelSharedSelector(),
				new RemotingClientChannelHandler(
						DefaultLoggerFactory.getDefault(), 
						new AtomicInteger(0)));
	}

	public void invoke_test() throws FormatterException, URISyntaxException,
			RemotingException {
		proxy.invoke(call);
	}
}