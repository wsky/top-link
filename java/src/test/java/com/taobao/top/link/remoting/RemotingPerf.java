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
import junit.textui.ResultPrinter;
import junit.textui.TestRunner;

import com.clarkware.junitperf.LoadTest;
import com.taobao.top.link.BufferManager;
//import com.clarkware.junitperf.TestMethodFactory;
import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.channel.ClientChannelSharedSelector;
import com.taobao.top.link.channel.ServerChannel;
import com.taobao.top.link.channel.websocket.WebSocketServerChannel;

@Ignore
public class RemotingPerf extends TestCase {
	private static URI uri;

	public static void main(String[] args) throws URISyntaxException {
		uri = new URI("ws://localhost:9000/");
		prepareServer(uri);

		int user = 100, per = 10000000;
		int total = user * per;

		// Test testCase = new TestMethodFactory(RemotingPerf.class,
		// "invoke_test");
		Test testCase = new RemotingPerf("invoke_test");
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
		handler.setSerializationFactory(new CrossLanguageSerializationFactory());
		ServerChannel serverChannel = new WebSocketServerChannel(uri.getPort(), true);
		serverChannel.setChannelHandler(handler);
		serverChannel.run();
	}

	private DynamicProxy proxy;
	private MethodCall call;

	public RemotingPerf(String name) throws URISyntaxException {
		super(name);

		call = new MethodCall();
		call.MethodSignature = new Class<?>[] { String.class };
		// 100byte message size
		BufferManager.setBufferSize(100);
		call.Args = new Object[] { "h" };
		// call.Args = new Object[] {
		// "123456789012345678901234567890123456789012345678901234567890" };

		RemotingClientChannelHandler handler = new RemotingClientChannelHandler(
				DefaultLoggerFactory.getDefault(),
				new AtomicInteger(0));
		handler.setSerializationFactory(new CrossLanguageSerializationFactory());

		proxy = new DynamicProxy(uri, new ClientChannelSharedSelector(), handler);
		proxy.setSerializationFormat("json");
	}

	public void invoke_test() throws FormatterException, URISyntaxException,
			RemotingException {
		proxy.invoke(call, 100);
	}
}