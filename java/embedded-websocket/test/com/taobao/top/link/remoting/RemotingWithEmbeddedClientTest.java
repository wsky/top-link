package com.taobao.top.link.remoting;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.channel.ClientChannelSelector;
import com.taobao.top.link.channel.embedded.EmbeddedClientChannelPooledSelector;
import com.taobao.top.link.channel.embedded.EmbeddedClientChannelSharedSelector;
import com.taobao.top.link.channel.websocket.WebSocketServerChannel;

public class RemotingWithEmbeddedClientTest {
	private static URI uri;
	private static WebSocketServerChannel serverChannel;
	private static ClientChannelSelector sharedSelector = new EmbeddedClientChannelSharedSelector();
	private static ClientChannelSelector pooledSelector = new EmbeddedClientChannelPooledSelector();
	private static RemotingClientChannelHandler remotingHandler = new RemotingClientChannelHandler(DefaultLoggerFactory.getDefault(), new AtomicInteger(0));

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
	public void invoke_test() throws FormatterException, RemotingException {
		((SampleInterface) new DynamicProxy(uri, sharedSelector, remotingHandler).create(SampleInterface.class, uri)).echo("hi");
		((SampleInterface) new DynamicProxy(uri, pooledSelector, remotingHandler).create(SampleInterface.class, uri)).echo("hi");
	}
}
