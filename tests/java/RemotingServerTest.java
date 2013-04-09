import java.net.URI;

import com.taobao.top.link.channel.websocket.WebSocketServerChannel;

import com.taobao.top.link.remoting.RemotingServerChannelHandler;
import com.taobao.top.link.remoting.MethodCall;
import com.taobao.top.link.remoting.MethodReturn;

public class RemotingServerTest {
	public static void main(String[] args) throws Exception {
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(9000);
		serverChannel.setChannelHandler(new RemotingServerChannelHandler() {
			@Override
			public MethodReturn onMethodCall(MethodCall methodCall) {
				MethodReturn methodReturn = new MethodReturn();
				methodReturn.ReturnValue = methodCall.Args[0];
				return methodReturn;
			}
		});
		serverChannel.run();
		System.in.read();
	}
}