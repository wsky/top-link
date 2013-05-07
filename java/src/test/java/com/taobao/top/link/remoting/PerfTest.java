package com.taobao.top.link.remoting;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.taobao.top.link.channel.ChannelException;

public class PerfTest {
	private int total = 10000;

	@Test
	public void remoting_test() throws FormatterException, URISyntaxException, RemotingException {
		// jmeter
		// RemotingService.connect(new URI("ws://localhost:9000/")).invoke(new
		// MethodCall());
	}

	// @Test
	public void remoting_sequence_test() throws URISyntaxException, ChannelException {
		String uriString = "ws://localhost:9030/";
		URI uri = new URI(uriString);
		this.runDefaultServer(uri);
		this.remoting_sequence_test(new URI(uriString + "sample"), total);
	}

	// @Test
	public void remoting_concurrent_test() throws URISyntaxException, ChannelException, InterruptedException {
		final String uriString = "ws://localhost:9031/";
		URI uri = new URI(uriString);
		this.runDefaultServer(uri);
		final URI remoteUri = new URI(uriString + "sample");
		for (int i = 0; i < 2; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						remoting_sequence_test(remoteUri, total);
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

	private void runDefaultServer(URI uri) {
		RemotingConfiguration.
				configure().
				businessThreadPool(new ThreadPoolExecutor(20, 100, 300, TimeUnit.SECONDS, new SynchronousQueue<Runnable>())).
				websocket(uri.getPort()).
				addProcessor("sample", new SampleService());
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
