package com.taobao.top.link.remoting;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map.Entry;

import org.junit.AfterClass;
import org.junit.Test;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.channel.ChannelContext;

public class ExtensionTest {
	@AfterClass
	public static void clear() {
		RemotingConfiguration.
				configure().
				defaultServerChannelHandler(new DefaultRemotingServerChannelHandler());
	}

	@Test(expected = Exception.class)
	public void cutsom_serverChannel_auth_fail_test() throws URISyntaxException {
		String uriString = "ws://localhost:9030/";
		URI uri = new URI(uriString);
		URI remoteUri = new URI(uriString + "sample");
		RemotingConfiguration.
				configure().
				defaultServerChannelHandler(new CustomServerChannelHandler()).
				websocket(uri.getPort()).
				addProcessor("sample", new SampleService());

		SampleServiceInterface sampleService = (SampleServiceInterface)
				RemotingService.connect(remoteUri, SampleServiceInterface.class);
		sampleService.echo("hi");
	}

	public class CustomServerChannelHandler extends DefaultRemotingServerChannelHandler {
		@SuppressWarnings("unchecked")
		public void onConnect(ChannelContext context) throws LinkException {
			Object msg = context.getMessage();
			if (msg instanceof List<?>) {
				List<Entry<String, String>> headers = (List<Entry<String, String>>) msg;
				for (Entry<String, String> entry : headers) {
					if (entry.getKey().equalsIgnoreCase("id"))
						return;
				}
			}
			throw new LinkException("dot not support, 不支持");
		}
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
