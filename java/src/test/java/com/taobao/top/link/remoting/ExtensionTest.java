package com.taobao.top.link.remoting;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.link.channel.websocket.WebSocketClientHelper;

public class ExtensionTest {
	private static URI remoteUri1;
	private static URI remoteUri2;
	private static CustomServerChannelHandler serverChannelHandler = new CustomServerChannelHandler();

	@BeforeClass
	public static void init() throws URISyntaxException {
		String uriString = "ws://localhost:9030/";
		URI uri = new URI(uriString);
		
		RemotingConfiguration.
				configure().
				defaultServerChannelHandler(serverChannelHandler).
				websocket(uri.getPort()).
				addProcessor("sample1", new SampleService()).
				addProcessor("sample2", new SampleService());

		remoteUri1 = new URI(uriString + "sample1");
		remoteUri2 = new URI(uriString + "sample2");
	}

	@AfterClass
	public static void clear() {
		RemotingConfiguration.
				configure().
				defaultServerChannelHandler(new DefaultRemotingServerChannelHandler());
	}

	@Test
	public void cutsom_serverChannel_test() throws URISyntaxException {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(CustomServerChannelHandler.ID, "abc");
		WebSocketClientHelper.setHeaders(remoteUri1, headers);
		SampleInterface sampleService = (SampleInterface)
				RemotingUtil.connect(remoteUri1, SampleInterface.class);
		assertEquals("hi", sampleService.echo("hi"));
	}

	@Test(expected = Exception.class)
	public void cutsom_serverChannel_auth_fail_test() throws URISyntaxException {
		SampleInterface sampleService = (SampleInterface)
				RemotingUtil.connect(remoteUri2, SampleInterface.class);
		sampleService.echo("hi");
	}
}
