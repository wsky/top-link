package com.taobao.top.link.remoting;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExtensionTest {
	private static URI remoteUri;

	@BeforeClass
	public static void init() throws URISyntaxException {
		String uriString = "ws://localhost:9030/";
		URI uri = new URI(uriString);
		remoteUri = new URI(uriString + "sample");
		RemotingConfiguration.
				configure().
				defaultServerChannelHandler(new CustomServerChannelHandler()).
				websocket(uri.getPort()).
				addProcessor("sample", new SampleService());
	}

	@AfterClass
	public static void clear() {
		RemotingConfiguration.
				configure().
				defaultServerChannelHandler(new DefaultRemotingServerChannelHandler());
	}

	@Test(expected = Exception.class)
	public void cutsom_serverChannel_auth_fail_test() throws URISyntaxException {
		SampleInterface sampleService = (SampleInterface)
				RemotingService.connect(remoteUri, SampleInterface.class);
		sampleService.echo("hi");
	}
}
