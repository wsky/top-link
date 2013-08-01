package com.taobao.top.link.endpoint;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import com.taobao.top.link.channel.websocket.WebSocketServerChannel;

public class EndpointRunner {
	public static void main(String[] args) throws URISyntaxException {
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
				context.reply(context.getMessage());
			}
		});		
	}
}
