package com.taobao.top.link.remoting;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.channel.ClientChannelSelector;
import com.taobao.top.link.channel.embedded.EmbeddedClientChannelPooledSelector;
import com.taobao.top.link.channel.embedded.EmbeddedClientChannelSharedSelector;

public class RemotingWithEmbeddedClientTest {
	private static URI uri;
	private static ClientChannelSelector sharedSelector = new EmbeddedClientChannelSharedSelector();
	private static ClientChannelSelector pooledSelector = new EmbeddedClientChannelPooledSelector();
	private static RemotingClientChannelHandler remotingHandler = new RemotingClientChannelHandler(DefaultLoggerFactory.getDefault(), new AtomicInteger(0));

	@BeforeClass
	public static void init() throws URISyntaxException {
		uri = new URI("ws://localhost:9050/sample");
		RemotingConfiguration.configure().websocket(uri.getPort()).addProcessor("sample", new SampleService());
	}

	@Test
	public void invoke_test() throws FormatterException, RemotingException {
		new DynamicProxy(uri, sharedSelector, remotingHandler).invoke(new MethodCall());
		new DynamicProxy(uri, pooledSelector, remotingHandler).invoke(new MethodCall());
	}
}
