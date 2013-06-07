import java.net.URI;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.channel.websocket.WebSocketServerChannel;
import com.taobao.top.link.remoting.RemotingServerChannelHandler;
import com.taobao.top.link.remoting.MethodCall;
import com.taobao.top.link.remoting.MethodCallContext;
import com.taobao.top.link.remoting.MethodReturn;
import com.taobao.top.link.remoting.CrossLanguageSerializationFactory;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RemotingServerTest {
	public static void main(String[] args) throws Exception {
		RemotingServerChannelHandler handler = new RemotingServerChannelHandler(new DefaultLoggerFactory()) {
			@Override
			public MethodReturn onMethodCall(MethodCall methodCall, MethodCallContext callContext) {
				MethodReturn methodReturn = new MethodReturn();
				methodReturn.ReturnValue = methodCall.Args[0];
				return methodReturn;
			}
		};
		handler.setSerializationFactory(new CrossLanguageSerializationFactory());
		handler.setThreadPool(new ThreadPoolExecutor(20, 200, 300, TimeUnit.SECONDS, new SynchronousQueue<Runnable>()));
		
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(9000, true);
		serverChannel.setChannelHandler(handler);
		serverChannel.run();
		System.out.println(String.format(
			"cumulative=true|threadpool=true|processors=%s", 
			Runtime.getRuntime().availableProcessors()));
		System.in.read();
	}
}