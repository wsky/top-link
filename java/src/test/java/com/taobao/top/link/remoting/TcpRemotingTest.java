package com.taobao.top.link.remoting;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.link.channel.tcp.TcpServerChannel;

public class TcpRemotingTest {
	private static URI uri;
	private static TcpServerChannel serverChannel;

	@BeforeClass
	public static void beforeClass() throws URISyntaxException {
		uri = new URI("tcp://localhost:8888/sample");
		DefaultRemotingServerChannelHandler serverHandler = new DefaultRemotingServerChannelHandler();
		serverHandler.addProcessor("sample", new SampleService());
		serverChannel = new NettyRemotingTcpServerChannel(uri.getPort());
		serverChannel.setChannelHandler(serverHandler);
		serverChannel.run();
	}

	@AfterClass
	public static void afterClass() {
		serverChannel.stop();
	}

	@Test
	public void call_test() {
		SampleInterface sampleService = (SampleInterface) RemotingUtil.connect(uri,
				SampleInterface.class,
				new NettyRemotingClientChannelSharedSelector());
		assertEquals("hi", sampleService.echo("hi"));
	}
}
