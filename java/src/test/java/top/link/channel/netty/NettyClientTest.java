package top.link.channel.netty;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import top.link.channel.ChannelException;
import top.link.channel.netty.NettyClient;
import top.link.channel.netty.NettyServerChannel;

public class NettyClientTest {
	static URI uri;
	static NettyServerChannel serverChannel;
	static ChannelGroup channelGroup;

	@BeforeClass
	public static void beforeClass() throws URISyntaxException {
		uri = new URI("tcp://localhost:8888/");
		serverChannel = new NettyServerChannel(uri.getPort()) {
			@Override
			protected void preparePipeline(ChannelPipeline pipeline) {
				pipeline.addLast("handler", new SimpleChannelUpstreamHandler());
			}
		};
		serverChannel.run();
		channelGroup = new DefaultChannelGroup();
	}

	@AfterClass
	public static void afterClass() {
		serverChannel.stop();
	}

	@After
	public void after() {
		channelGroup.close();
	}

	@Test
	public void connect_test() throws ChannelException {
		channelGroup.add(NettyClient.prepareAndConnect(null, uri,
				Channels.pipeline(new SimpleChannelUpstreamHandler()), null, false, 1000));
	}

	@Test
	public void connect_fail_test() throws ChannelException, URISyntaxException {
		int prev = Thread.getAllStackTraces().keySet().size();
		System.out.println(prev);

		for (int i = 0; i < 5; i++)
			try {
				NettyClient.prepareAndConnect(null, new URI("tcp://localhost:9080/"),
						Channels.pipeline(new SimpleChannelUpstreamHandler()), null, false, 100);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

		System.out.println(Thread.getAllStackTraces().keySet().size());
	}

	@Test
	public void threadpool_test() throws ChannelException {
		int prev = Thread.getAllStackTraces().keySet().size();
		System.out.println(prev);

		int c = 5;
		for (int i = 0; i < c; i++)
			channelGroup.add(NettyClient.prepareAndConnect(null, uri,
					Channels.pipeline(new SimpleChannelUpstreamHandler()), null, false, 1000));

		System.out.println(Thread.getAllStackTraces().keySet().size());
		assertEquals(prev, Thread.getAllStackTraces().keySet().size());
		// assertTrue(Thread.getAllStackTraces().keySet().size() - prev <= c * 2);
	}

	public static void main(String[] args) throws URISyntaxException, ChannelException {
		beforeClass();

		for (int i = 0; i < 100; i++)
			channelGroup.add(NettyClient.prepareAndConnect(null, uri,
					Channels.pipeline(new SimpleChannelUpstreamHandler()), null, false, 1000));

	}

}
