package top.link.channel.websocket;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import top.link.ResetableTimer;
import top.link.channel.ChannelContext;
import top.link.channel.ChannelException;
import top.link.channel.ChannelHandler;
import top.link.channel.ClientChannel;
import top.link.channel.ServerChannelSender;
import top.link.channel.websocket.WebSocketClient;
import top.link.channel.websocket.WebSocketClientChannel;

public class WebSocketChannelTest {
	private static URI uri;
	private static URI uriSsl;
	private static WebSocketServerChannelWrapper serverChannelWrapper;
	private static ServerChannelSender connectedSender;
	
	@BeforeClass
	public static void init() throws URISyntaxException {
		uri = new URI("ws://localhost:8888/");
		uriSsl = new URI("wss://localhost:8888/");
		
		serverChannelWrapper = new WebSocketServerChannelWrapper(uri.getPort());
		serverChannelWrapper.run();
		serverChannelWrapper.setChannelHandler(new ChannelHandler() {
			public void onMessage(ChannelContext context) throws Exception {
			}
			
			public void onError(ChannelContext context) throws Exception {
			}
			
			public void onConnect(ChannelContext context) throws Exception {
				connectedSender = (ServerChannelSender) context.getSender();
				System.out.println(connectedSender.getLocalAddress());
				System.out.println(connectedSender.getRemoteAddress());
			}
			
			public void onClosed(String reason) {
			}
		});
	}
	
	@AfterClass
	public static void afterClass() {
		serverChannelWrapper.stop();
	}
	
	@After
	public void after() {
		serverChannelWrapper.setSSLContext(null);
	}
	
	@Test
	public void connect_80_test() throws URISyntaxException, ChannelException {
		WebSocketClient.parse(new URI("ws://localhost/"));
		WebSocketClient.parse(new URI("ws://localhost"));
	}
	
	@Test(expected = ChannelException.class)
	public void connect_timeout_test() throws URISyntaxException, ChannelException {
		try {
			WebSocketClient.connect(new URI("ws://10.10.1.200:8889"), 100);
		} catch (ChannelException e) {
			throw e;
		}
	}
	
	@Test
	public void heartbeat_test() throws ChannelException, InterruptedException {
		heartbeat_test(uri);
	}
	
	@Test
	public void ssl_test() throws ChannelException, InterruptedException {
		serverChannelWrapper.ssl();
		heartbeat_test(uriSsl);
	}
	
	@Test
	public void client_close_test() throws ChannelException, URISyntaxException, InterruptedException {
		ClientChannel clientChannel = WebSocketClient.connect(uri, 1000);
		assertTrue(clientChannel.isConnected());
		
		clientChannel.close("close");
		Thread.sleep(100);
		assertFalse(clientChannel.isConnected());
		// server got closeframe and than disconnected->closed
	}
	
	@Test
	public void client_unexpected_close_test() throws ChannelException, InterruptedException {
		WebSocketClientChannel clientChannel = (WebSocketClientChannel) WebSocketClient.connect(uri, 1000);
		assertTrue(clientChannel.isConnected());
		
		clientChannel.getChannel().disconnect().syncUninterruptibly();
		// same as
		// clientChannel.channel.close().syncUninterruptibly();
		Thread.sleep(100);
		assertFalse(clientChannel.isConnected());
		// server got disconnected->closed
		// if FIN not received, server wont know about it
	}
	
	@Test
	public void server_close_test() throws ChannelException, InterruptedException {
		ClientChannel clientChannel = WebSocketClient.connect(uri, 1000);
		assertTrue(clientChannel.isConnected());
		// server close
		connectedSender.close("close");
		Thread.sleep(100);
		assertFalse(clientChannel.isConnected());
		assertFalse(connectedSender.isOpen());
	}
	
	@Test
	public void server_unexpected_close_test() throws ChannelException, InterruptedException {
		ClientChannel clientChannel = WebSocketClient.connect(uri, 1000);
		assertTrue(clientChannel.isConnected());
		// server close
		serverChannelWrapper.stop();
		Thread.sleep(100);
		assertFalse(clientChannel.isConnected());
		assertFalse(connectedSender.isOpen());
	}
	
	private void heartbeat_test(URI uri) throws ChannelException, InterruptedException {
		ClientChannel clientChannel = WebSocketClient.connect(uri, 1000);
		serverChannelWrapper.handlerWrapper.latch = new CountDownLatch(3);
		ResetableTimer timer = new ResetableTimer(100);
		clientChannel.setHeartbeatTimer(timer);
		serverChannelWrapper.handlerWrapper.latch.await();
		timer.stop();
	}
}
