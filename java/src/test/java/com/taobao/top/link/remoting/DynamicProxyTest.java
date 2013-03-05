package com.taobao.top.link.remoting;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Test;

import com.taobao.top.link.ChannelException;
import com.taobao.top.link.Endpoint;
import com.taobao.top.link.websocket.WebSocketServerChannel;

// high-level abstract remoting test
public class DynamicProxyTest {
	public void dynamicProxy_test() throws Exception {
		DefaultRemotingServerChannelHandler remotingServerChannelHandler = new DefaultRemotingServerChannelHandler();
		remotingServerChannelHandler.addService(new SampleService());

		URI uri = new URI("ws://localhost:9005/link");
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(uri.getHost(), uri.getPort());
		final Endpoint server = new Endpoint();
		server.setChannelHandler(remotingServerChannelHandler);
		server.bind(serverChannel);

		//SampleServiceInterface sampleService = (SampleServiceInterface) RemotingService.connect(uri, SampleServiceInterface.class);
		//assertEquals("hi", sampleService.echo("hi"));
	}
}
