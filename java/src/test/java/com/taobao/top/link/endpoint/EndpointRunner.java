package com.taobao.top.link.endpoint;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Map;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.channel.websocket.WebSocketServerChannel;
import com.taobao.top.link.util.GZIPHelper;

public class EndpointRunner {
	public static void main(String[] args) throws URISyntaxException {
		DefaultLoggerFactory.setDefault(true, true, true, true, true);
		URI uri = new URI("ws://localhost:9090/");
		Endpoint e = new Endpoint(new DefaultIdentity("echo_server"));
		e.bind(new WebSocketServerChannel(uri.getPort()));
		e.setMessageHandler(new MessageHandler() {
			@Override
			public void onMessage(Map<String, Object> message, Identity messageFrom) {
				System.out.println(message);
			}

			@Override
			public void onMessage(EndpointContext context) throws Exception {
				System.out.println(context.getMessageFrom() + "|" + context.getMessage());
				if (context.getMessage().containsKey("byte[]")) {
					byte[] data = (byte[]) context.getMessage().get("byte[]");
					for (byte b : data) {
						System.out.print(b);
						System.out.print(",");
					}
					System.out.println();
					String value = new String(GZIPHelper.unzip(data), Charset.forName("UTF-8"));
					System.out.println(value);
					context.getMessage().put("byte[]", GZIPHelper.zip(value.getBytes(Charset.forName("UTF-8"))));
				}
				context.reply(context.getMessage());
			}
		});
	}
}
