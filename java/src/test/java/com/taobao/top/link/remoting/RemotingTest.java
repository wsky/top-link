package com.taobao.top.link.remoting;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.LoggerFactory;
import com.taobao.top.link.channel.ChannelException;
import com.taobao.top.link.channel.websocket.WebSocketServerChannel;

public class RemotingTest {
	private static LoggerFactory loggerFactory = new DefaultLoggerFactory(true, true, true, true, true);

	@BeforeClass
	public static void init() {
		RemotingService.setLoggerFactory(loggerFactory);
	}
	
	@Test
	public void send_test() throws URISyntaxException, ChannelException {
		URI uri = new URI("ws://localhost:9001/link");
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(loggerFactory, uri.getPort());
		serverChannel.setChannelHandler(new RemotingServerChannelHandler() {
			@Override
			public MethodReturn onMethodCall(MethodCall methodCall) {
				MethodReturn methodReturn = new MethodReturn();
				methodReturn.ReturnValue = "ok";
				return methodReturn;
			}
		});
		serverChannel.run();

		DynamicProxy proxy = RemotingService.connect(uri);
		MethodCall methodCall = new MethodCall();
		MethodReturn methodReturn = null;
		try {
			methodReturn = proxy.invoke(methodCall);
		} catch (RemotingException e) {
			e.printStackTrace();
		} catch (FormatterException e) {
			e.printStackTrace();
		}
		assertNull(methodReturn.Exception);
		assertEquals("ok", methodReturn.ReturnValue);
	}

	@Test(expected = RemotingException.class)
	public void execution_timeout_test() throws URISyntaxException, ChannelException, RemotingException, FormatterException {
		URI uri = new URI("ws://localhost:9003/link");
		WebSocketServerChannel serverChannel = new WebSocketServerChannel(loggerFactory, uri.getPort());
		serverChannel.setChannelHandler(new RemotingServerChannelHandler() {
			@Override
			public MethodReturn onMethodCall(MethodCall methodCall) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return null;
			}
		});
		serverChannel.run();

		DynamicProxy proxy = RemotingService.connect(uri);

		try {
			proxy.invoke(new MethodCall(), 2000);
		} catch (RemotingException e) {
			assertEquals("remoting execution timeout", e.getMessage());
			throw e;
		}
	}

	@Test(expected = RemotingException.class)
	public void channel_broken_while_calling_test() throws Throwable {
		URI uri = new URI("ws://localhost:9004/link");
		final WebSocketServerChannel serverChannel = new WebSocketServerChannel(loggerFactory, uri.getPort());
		serverChannel.setChannelHandler(new RemotingServerChannelHandler() {
			@Override
			public MethodReturn onMethodCall(MethodCall methodCall) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return null;
			}
		});
		serverChannel.run();

		DynamicProxy proxy = RemotingService.connect(uri);

		// make server broken
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				serverChannel.stop();
			}
		}).start();

		try {
			proxy.invoke(new MethodCall());
		} catch (RemotingException e) {
			assertEquals("channel broken with unknown error", e.getMessage());
			throw e;
		}
	}

	@Test
	public void transportHeaders_got_error_statusCode_test() {

	}
}
