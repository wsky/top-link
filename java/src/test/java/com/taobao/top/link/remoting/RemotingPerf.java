package com.taobao.top.link.remoting;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;

import junit.framework.TestCase;

import com.clarkware.junitperf.LoadTest;
import com.taobao.top.link.channel.ClientChannelSharedSelector;
import com.taobao.top.link.channel.websocket.WebSocketServerChannel;

@Ignore
public class RemotingPerf extends TestCase {
	public static void main(String[] args) throws URISyntaxException {
		int user = 10, per = 10000;
		int total = user * per;

		RemotingPerf testCase = new RemotingPerf("invoke_test");
		LoadTest loadTest = new LoadTest(testCase, user, per);
		// TimedTest timedTest = new TimedTest(loadTest, 10000, false);

		long begin = System.currentTimeMillis();
		junit.textui.TestRunner.run(loadTest);
		long cost = System.currentTimeMillis() - begin;
		System.out.println(String.format(
				"total:%s, cost:%sms, tps:%scall/s, time:%sms", total, cost,
				((float) total / (float) cost) * 1000, (float) cost
						/ (float) total));

		testCase.clear();
		System.exit(0);
	}

	private WebSocketServerChannel serverChannel;
	private DynamicProxy proxy;
	private MethodCall call;

	public RemotingPerf(String name) throws URISyntaxException {
		super(name);
		
		URI uri = new URI("ws://localhost:8080/");
		call = new MethodCall();
		call.Args = new Object[] { "hello1234567890123456789123456789hello1234567890123456789123456789" };
		
		this.prepareServer(uri);
		
		RemotingService.setChannelSelector(new ClientChannelSharedSelector());
		proxy = RemotingService.connect(uri);
	}

	public void invoke_test() throws FormatterException, URISyntaxException,
			RemotingException {
		proxy.invoke(call);
	}

	public void clear() {
		this.serverChannel.stop();
	}

	private void prepareServer(URI uri) {
		RemotingServerChannelHandler handler = new RemotingServerChannelHandler() {
			@Override
			public MethodReturn onMethodCall(MethodCall methodCall, MethodCallContext callContext) {
				MethodReturn methodReturn = new MethodReturn();
				methodReturn.ReturnValue = methodCall.Args[0];
				return methodReturn;
			}
		};
		handler.setThreadPool(new ThreadPoolExecutor(20, 200, 300, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()));
		this.serverChannel = new WebSocketServerChannel(uri.getPort(), true);
		this.serverChannel.setChannelHandler(handler);
		this.serverChannel.run();
	}
}