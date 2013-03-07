package com.taobao.top.link.remoting;

import static org.junit.Assert.*;

import java.net.URI;

import org.junit.Test;

import com.taobao.top.link.Endpoint;
import com.taobao.top.link.websocket.WebSocketServerChannel;

// high-level abstract remoting test
public class DynamicProxyTest {
	@Test
	public void dynamicProxy_test() throws Throwable {
		URI uri = new URI("ws://localhost:9020/sample");
		DefaultRemotingServerChannelHandler handler = this.runDefaultServer(uri);
		handler.addProcessor("sample", new SampleService());

		SampleServiceInterface sampleService = (SampleServiceInterface) RemotingService.connect(uri, SampleServiceInterface.class);
		assertEquals("hi", sampleService.echo("hi"));
	}

	@Test(expected = RemotingException.class)
	public void invoke_throw_not_UndeclaredThrowable_test() throws RemotingException, Exception {
		URI uri = new URI("ws://localhost:9021/sample");
		DefaultRemotingServerChannelHandler handler = this.runDefaultServer(uri);
		handler.addProcessor("sample", new SampleService());

		DynamicProxy proxy = RemotingService.connect(uri);

		MethodCall methodCall = new MethodCall();
		methodCall.Args = new Object[] { "hi" };

		MethodReturn methodReturn = proxy.invoke(methodCall);
		throw new RemotingException("", methodReturn.Exception);
	}

	private DefaultRemotingServerChannelHandler runDefaultServer(URI uri) {
		DefaultRemotingServerChannelHandler remotingServerChannelHandler = new DefaultRemotingServerChannelHandler();
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(uri.getHost(), uri.getPort());
		final Endpoint server = new Endpoint();
		server.setChannelHandler(remotingServerChannelHandler);
		server.bind(serverChannel);
		return remotingServerChannelHandler;
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
