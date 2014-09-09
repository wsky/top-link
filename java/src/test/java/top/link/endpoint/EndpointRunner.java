package top.link.endpoint;

import java.net.URI;
import java.net.URISyntaxException;

import top.link.channel.websocket.WebSocketServerChannel;
import top.link.endpoint.DefaultIdentity;
import top.link.endpoint.Endpoint;
import top.link.endpoint.EndpointBaseContext;
import top.link.endpoint.EndpointContext;
import top.link.endpoint.MessageHandler;
import top.link.util.GZIPHelper;

public class EndpointRunner {
	public static void main(String[] args) throws URISyntaxException {
		URI uri = new URI("ws://localhost:9090/");
		Endpoint e = new Endpoint(new DefaultIdentity("echo_server"));
		e.bind(new WebSocketServerChannel(uri.getPort()));
		e.setMessageHandler(new MessageHandler() {
			public void onAckMessage(EndpointBaseContext context) {
				System.out.println(context.getMessage());
			}

			public void onMessage(EndpointContext context) throws Exception {
				System.out.println(context.getMessageFrom() + "|" + context.getMessage());
				if (context.getMessage().containsKey("byte[]")) {
					byte[] data = (byte[]) context.getMessage().get("byte[]");
					for (byte b : data) {
						System.out.print(b);
						System.out.print(",");
					}
					System.out.println();
					String value = new String(GZIPHelper.unzip(data), "UTF-8");
					System.out.println(value);
					context.getMessage().put("byte[]", GZIPHelper.zip(value.getBytes("UTF-8")));
				}
				context.reply(context.getMessage());
			}
		});
	}
}
