package top.link.remoting;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import top.link.channel.websocket.WebSocketClientHelper;
import top.link.channel.websocket.WebSocketServerChannel;

public class ExtensionTest {
	private static URI uri;
	private static WebSocketServerChannel serverChannel;
	private static CustomServerChannelHandler customServerChannelHandler = new CustomServerChannelHandler();

	@BeforeClass
	public static void init() throws URISyntaxException {
		uri = new URI("ws://localhost:8888/sample");
		customServerChannelHandler.addProcessor("sample", new SampleService());
		serverChannel = new WebSocketServerChannel(uri.getPort());
		serverChannel.setChannelHandler(customServerChannelHandler);
		serverChannel.run();
	}

	@AfterClass
	public static void clear() {
		serverChannel.stop();
	}

	@Test
	public void cutsom_serverChannel_test() throws URISyntaxException {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(CustomServerChannelHandler.ID, "abc");
		WebSocketClientHelper.setHeaders(uri, headers);
		SampleInterface sampleService = (SampleInterface) RemotingUtil.connect(uri, SampleInterface.class);
		assertEquals("hi", sampleService.echo("hi"));
	}

	@Test(expected = Exception.class)
	public void cutsom_serverChannel_auth_fail_test() throws URISyntaxException {
		WebSocketClientHelper.setHeaders(uri, null);
		SampleInterface sampleService = (SampleInterface) RemotingUtil.connect(uri, SampleInterface.class);
		sampleService.echo("hi");
	}
}
