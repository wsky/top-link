package com.taobao.top.link;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import com.taobao.top.link.websocket.WebSocketServerChannel;

public class IdentityTest {
	private LoggerFactory loggerFactory = new DefaultLoggerFactory(true, true, true, true, true);

	@Test
	public void connect_with_id_test() throws URISyntaxException, ChannelException {
		URI uri = new URI("ws://localhost:9040/");
		Endpoint app1 = runEndpoint(uri);

		TopIdentity id = new TopIdentity("app2");
		Endpoint app2 = new Endpoint(id);
		app2.getEndpoint(uri);

		assertEquals(TopIdentity.class, app1.getConnected().next().getIdentity().getClass());
		assertEquals("app2", app1.getConnected().next().getIdentity().toString());
		assertNotNull(app1.getEndpoint(id));
	}

	@Test(expected = ChannelException.class)
	public void connect_with_wrong_id_test() throws URISyntaxException, ChannelException {
		URI uri = new URI("ws://localhost:9041/");
		runEndpoint(uri);

		try {
			new Endpoint(new TopIdentity("")).getEndpoint(uri);
		} catch (ChannelException e) {
			assertEquals("connect fail: Invalid handshake response", e.getMessage());
			throw e;
		}
	}

	private Endpoint runEndpoint(URI uri) {
		return runEndpoint(uri, "appkey");
	}

	private Endpoint runEndpoint(URI uri, String appkey) {
		Endpoint endpoint = new Endpoint(new TopIdentity(appkey));
		endpoint.bind(new WebSocketServerChannel(loggerFactory, uri.getPort()));
		return endpoint;
	}

	class TopIdentity implements Identity {
		public String appKey;

		public TopIdentity(String appkey) {
			this.appKey = appkey;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Identity parse(Object data) throws LinkException {
			TopIdentity identity = new TopIdentity(null);
			List<Entry<String, String>> headers = (List<Entry<String, String>>) data;
			for (Entry<String, String> entry : headers) {
				if (entry.getKey().equalsIgnoreCase("appkey") && entry.getValue() != "") {
					identity.appKey = entry.getValue();
					return identity;
				}
			}
			throw new LinkException(401, "id error");
		}

		@SuppressWarnings("unchecked")
		@Override
		public void render(Object to) {
			((Map<String, String>) to).put("appkey", this.appKey);
		}

		@Override
		public boolean equals(Identity id) {
			return ((TopIdentity) id).appKey.equals(this.appKey);
		}

		@Override
		public String toString() {
			return this.appKey;
		}
	}
}