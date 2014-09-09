package top.link.remoting;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import top.link.channel.ChannelException;
import top.link.channel.ClientChannelPooledSelector;
import top.link.channel.ClientChannelSharedSelector;
import top.link.channel.websocket.WebSocketServerChannel;
import top.link.remoting.DynamicProxy;
import top.link.remoting.FormatterException;
import top.link.remoting.MethodCall;
import top.link.remoting.MethodCallContext;
import top.link.remoting.MethodReturn;
import top.link.remoting.RemotingException;
import top.link.remoting.RemotingServerChannelHandler;

// rpc timing is important for overlay-io/reused-channel
public class TimingTest {
	private static URI uri;
	private static WebSocketServerChannel serverChannel;
	private ClientChannelSharedSelector sharedSelector = new ClientChannelSharedSelector();
	private ClientChannelPooledSelector pooledSelector = new ClientChannelPooledSelector();

	@BeforeClass
	public static void init() throws URISyntaxException {
		uri = new URI("ws://localhost:8888/");
		serverChannel = new WebSocketServerChannel(uri.getPort());
		serverChannel.setChannelHandler(new RemotingServerChannelHandler() {
			@Override
			public MethodReturn onMethodCall(MethodCall methodCall, MethodCallContext callContext) {
				MethodReturn methodReturn = new MethodReturn();
				methodReturn.ReturnValue = methodCall.Args[0];
				return methodReturn;
			}
		});
		serverChannel.run();
	}

	@AfterClass
	public static void clear() {
		serverChannel.stop();
	}

	@Test
	public void timing_test() throws URISyntaxException, RemotingException, FormatterException, ChannelException {
		DynamicProxy proxy = RemotingUtil.connect(uri);
		for (int i = 0; i < 10; i++) {
			MethodCall methodCall = new MethodCall();
			methodCall.Args = new Object[] { i };
			assertEquals(i, proxy.invoke(methodCall).ReturnValue);
		}
	}

	@Test
	public void same_channel_timing_test() throws URISyntaxException, ChannelException, InterruptedException {
		// proxy1/2 will share same channel
		sharedSelector.getChannel(uri);
		final DynamicProxy proxy1 = RemotingUtil.connect(uri, sharedSelector);
		final DynamicProxy proxy2 = RemotingUtil.connect(uri, sharedSelector);

		this.runThread(proxy1, uri, 0, 100);
		this.runThread(proxy2, uri, 100, 200);

		synchronized (uri) {
			uri.wait(10000);
		}
	}

	@Test
	public void different_channel_timing_test() throws URISyntaxException, ChannelException, InterruptedException {
		// proxy1/2 use different channel but same remote server
		final DynamicProxy proxy1 = RemotingUtil.connect(uri, pooledSelector);
		final DynamicProxy proxy2 = RemotingUtil.connect(uri, pooledSelector);

		this.runThread(proxy1, uri, 0, 100);
		this.runThread(proxy2, uri, 100, 200);

		synchronized (uri) {
			uri.wait(10000);
		}
	}

	private void runThread(final DynamicProxy proxy, final Object sync, final int from, final int to) {
		new Thread(new Runnable() {
			public void run() {
				try {
					for (int i = from; i < to; i++) {
						MethodCall methodCall = new MethodCall();
						methodCall.Args = new Object[] { i };
						assertEquals(i, proxy.invoke(methodCall).ReturnValue);
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}

				synchronized (sync) {
					sync.notify();
				}
			}
		}).start();
	}
}
