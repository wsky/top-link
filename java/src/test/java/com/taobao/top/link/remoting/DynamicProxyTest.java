package com.taobao.top.link.remoting;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.link.channel.websocket.WebSocketServerChannel;

// high-level abstract remoting test
public class DynamicProxyTest {
	private static URI uri;
	private static WebSocketServerChannel serverChannel;

	@BeforeClass
	public static void init() throws URISyntaxException {
		uri = new URI("ws://localhost:8888/sample");
		DefaultRemotingServerChannelHandler serverHandler = new DefaultRemotingServerChannelHandler();
		serverHandler.addProcessor("sample", new SampleService());
		serverChannel = new WebSocketServerChannel(uri.getPort());
		serverChannel.setChannelHandler(serverHandler);
		serverChannel.run();
	}

	@AfterClass
	public static void clear() {
		serverChannel.stop();
	}

	@Test
	public void dynamicProxy_test() throws Throwable {
		// java.lang.IllegalArgumentException:
		// com.taobao.top.link.remoting.DynamicProxyTest$SampleService is not an
		// interface
		// SampleService sampleService = (SampleService)
		// RemotingService.connect(uri, SampleService.class);
		SampleInterface sampleService = (SampleInterface)
				RemotingService.connect(uri, SampleInterface.class);
		assertEquals("hi", sampleService.echo("hi"));
	}

	@Test(expected = NullPointerException.class)
	public void objectUri_empty_or_not_matched_processor_test() throws Throwable {
		URI remoteUri = new URI(uri.toString() + "_wrong");
		SampleInterface sampleService = (SampleInterface)
				RemotingService.connect(remoteUri, SampleInterface.class);
		try {
			sampleService.echo("hi");
		} catch (Exception e) {
			assertEquals("processor not found for objectUri: /sample_wrong", e.getMessage());
			throw e;
		}
	}

	@Test(expected = RemotingException.class)
	public void invoke_throw_not_UndeclaredThrowable_test() throws RemotingException, Exception {
		DynamicProxy proxy = RemotingService.connect(uri);
		MethodCall methodCall = new MethodCall();
		methodCall.Args = new Object[] { "hi" };
		MethodReturn methodReturn = proxy.invoke(methodCall);
		throw new RemotingException("", methodReturn.Exception);
	}

	@Test
	public void multi_thread_test() throws URISyntaxException, InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1000);
		final AtomicBoolean flag = new AtomicBoolean(true);
		for (int i = 0; i < 4; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (flag.get()) {
						try {
							SampleInterface sampleService = (SampleInterface)
									RemotingService.connect(uri, SampleInterface.class);
							sampleService.echo("hi");
							latch.countDown();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		}
		latch.await();
		flag.set(false);
	}
}
