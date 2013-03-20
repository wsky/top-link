package com.taobao.top.link.remoting;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.taobao.top.link.ChannelException;
import com.taobao.top.link.Endpoint;
import com.taobao.top.link.websocket.WebSocketServerChannel;

public class PerfTest {
	private static DynamicProxy proxy;

	//@BeforeClass
	public static void connect() throws URISyntaxException, ChannelException {
		if (proxy == null) {
			URI uri = new URI("ws://localhost:9000/");
			proxy = RemotingService.connect(uri);
		}
	}

	@Test
	public void remoting_test() throws FormatterException, RemotingException, URISyntaxException, ChannelException {
		//connect();// for jmeter
		//proxy.invoke(new MethodCall());
	}

	public void remoting_sequence_test() throws URISyntaxException, ChannelException {
		String uriString = "ws://localhost:9030/";
		URI uri = new URI(uriString);
		DefaultRemotingServerChannelHandler handler = this.runDefaultServer(uri);
		handler.addProcessor("sample", new SampleService());
		this.remoting_sequence_test(new URI(uriString + "sample"), 100000);
	}

	public void remoting_concurrent_test() throws URISyntaxException, ChannelException, InterruptedException {
		final String uriString = "ws://localhost:9031/";
		URI uri = new URI(uriString);
		DefaultRemotingServerChannelHandler handler = this.runDefaultServer(uri);
		handler.addProcessor("sample", new SampleService());
		final URI remoteUri = new URI(uriString + "sample");
		for (int i = 0; i < 2; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						remoting_sequence_test(remoteUri, 100000);
					} catch (ChannelException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
		Thread.sleep(30000);
	}

	@Test
	public void remoting_OOM_test() {

	}

	private void remoting_sequence_test(URI remoteUri, long total) throws ChannelException {
		// 10ms min
		long begin = System.currentTimeMillis();
		for (int i = 0; i < total; i++) {
			SampleServiceInterface sampleService = (SampleServiceInterface)
					RemotingService.connect(remoteUri, SampleServiceInterface.class);
			sampleService.echo("hi");
		}
		long cost = System.currentTimeMillis() - begin;
		System.out.println(String.format(
				"total:%s, cost:%sms, tps:%scall/s, time:%sms", total, cost,
				((float) total / (float) cost) * 1000,
				(float) cost / (float) total));
		// total:100000, cost:18219ms, tps:5488.7754call/s, time:0.18219ms
	}

	private DefaultRemotingServerChannelHandler runDefaultServer(URI uri) {
		DefaultRemotingServerChannelHandler remotingServerChannelHandler = new DefaultRemotingServerChannelHandler();
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(uri.getHost(), uri.getPort());
		final Endpoint server = new Endpoint();
		server.setChannelHandler(remotingServerChannelHandler);
		server.bind(serverChannel);
		return remotingServerChannelHandler;
	}

	public interface SampleServiceInterface {
		public String echo(String input);
	}

	public class SampleService extends DefaultMethodCallProcessor implements SampleServiceInterface {
		@Override
		public String echo(String input) {
			return input;
		}
	}
}
