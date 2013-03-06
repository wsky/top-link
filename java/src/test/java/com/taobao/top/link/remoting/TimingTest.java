package com.taobao.top.link.remoting;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.junit.Test;

import com.taobao.top.link.ChannelException;
import com.taobao.top.link.ClientChannel;
import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.Endpoint;
import com.taobao.top.link.websocket.WebSocketChannelSelectHandler;
import com.taobao.top.link.websocket.WebSocketServerChannel;

// rpc timing is important for overlay-io/reused-channel
public class TimingTest {
	private WebSocketChannelSelectHandler selectHandler = new WebSocketChannelSelectHandler(new DefaultLoggerFactory());

	@Test
	public void timing_test() throws URISyntaxException, ChannelException, InterruptedException {
		final URI uri = new URI("ws://localhost:9010/link");
		this.runServer(uri);

		byte[] data = new byte[4];
		DynamicProxy proxy = RemotingService.connect(uri);
		for (int i = 0; i < 10; i++) {
			ByteBuffer.wrap(data).putInt(i);
			//ByteBuffer resultBuffer = proxy.send(data, 0, 4);
			//assertEquals(i, resultBuffer.getInt());
		}
	}

	@Test
	public void same_channel_timing_test() throws URISyntaxException, ChannelException, InterruptedException {
		final URI uri = new URI("ws://localhost:9011/link");
		this.runServer(uri);

		// proxy1/2 will share same channel
		ClientChannel channel = selectHandler.getClientChannel(uri);
		final DynamicProxy proxy1 = RemotingService.proxy(channel);
		final DynamicProxy proxy2 = RemotingService.proxy(channel);

		this.runThread(proxy1, uri, 0, 100);
		this.runThread(proxy2, uri, 100, 200);

		synchronized (uri) {
			uri.wait(10000);
		}
	}

	@Test
	public void different_channel_timing_test() throws URISyntaxException, ChannelException, InterruptedException {
		final URI uri = new URI("ws://localhost:9012/link");
		this.runServer(uri);

		// proxy1/2 use different channel but same remote server
		final DynamicProxy proxy1 = RemotingService.proxy(selectHandler.connect(uri, 5000, null));
		final DynamicProxy proxy2 = RemotingService.proxy(selectHandler.connect(uri, 5000, null));

		this.runThread(proxy1, uri, 0, 100);
		this.runThread(proxy2, uri, 100, 200);

		synchronized (uri) {
			uri.wait(10000);
		}
	}

	private void runThread(final DynamicProxy proxy, final Object sync, final int from, final int to) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					byte[] data = new byte[4];
					for (int i = from; i < to; i++) {
						ByteBuffer.wrap(data).putInt(i);
						//ByteBuffer resultBuffer = proxy.send(data, 0, 4);
						//assertEquals(i, resultBuffer.getInt());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				synchronized (sync) {
					sync.notify();
				}
			}
		}).start();
	}

	private void runServer(URI uri) {
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(uri.getHost(), uri.getPort());
		Endpoint server = new Endpoint();
		server.setChannelHandler(new RemotingServerChannelHandler() {
			@Override
			public void onRequest(ByteBuffer requestBuffer, ByteBuffer responseBuffer) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				responseBuffer.put(new byte[] {
						requestBuffer.get(),
						requestBuffer.get(),
						requestBuffer.get(),
						requestBuffer.get() });
			}
		});
		server.bind(serverChannel);
	}
}
