import java.net.URI;

import com.taobao.top.link.Endpoint;
import com.taobao.top.link.websocket.WebSocketChannelSelectHandler;
import com.taobao.top.link.websocket.WebSocketServerChannel;

import com.taobao.top.link.remoting.RemotingServerChannelHandler;
import com.taobao.top.link.remoting.MethodCall;
import com.taobao.top.link.remoting.MethodReturn;

public class RemotingServerTest {
	public static void main(String[] args) throws Exception {
		URI uri = new URI("ws://localhost:9000/");
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(uri.getHost(), uri.getPort());
		Endpoint server = new Endpoint();
		server.setChannelHandler(new RemotingServerChannelHandler() {
			@Override
			public MethodReturn onMethodCall(MethodCall methodCall) {
				MethodReturn methodReturn = new MethodReturn();
				methodReturn.ReturnValue = methodCall.Args[0];
				return methodReturn;
			}
		});
		server.bind(serverChannel);
		System.in.read();
	}
}