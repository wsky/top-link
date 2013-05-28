package com.taobao.top.link.remoting;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Ignore;

import junit.framework.TestCase;

import com.clarkware.junitperf.LoadTest;
import com.taobao.top.link.channel.ClientChannelSharedSelector;

@Ignore
public class RemotingPerf extends TestCase {
	private static int total = 10000;
	private static URI uri;
	private static DynamicProxy proxy;
	private static MethodCall call;

	static {
		RemotingService.setChannelSelector(new ClientChannelSharedSelector());
		try {
			uri = new URI("ws://localhost:9000/");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		proxy = RemotingService.connect(uri);
		call = new MethodCall();
		call.Args = new Object[] { "hello1234567890123456789123456789hello1234567890123456789123456789" };
	}

	public RemotingPerf(String name) {
		super(name);
	}

	public static void main(String[] args) throws FormatterException,
			RemotingException {
		proxy.invoke(call);

		int user = 100, per = 10000;
		total = user * per;

		RemotingPerf testCase = new RemotingPerf("remoting_test");
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

	public void remoting_test() throws FormatterException, URISyntaxException,
			RemotingException {
		proxy.invoke(call);
	}
}