package top.link.channel.tcp;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.Channels;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import top.link.channel.ChannelContext;
import top.link.channel.ChannelException;
import top.link.channel.ChannelHandler;
import top.link.channel.ClientChannel;
import top.link.channel.tcp.TcpClient;

public class TcpChannelTest {
	private static URI uri;
	private static URI uriSsl;
	private static TcpServerChannelWrapper serverChannelWrapper;
	private static CountDownLatch latch = new CountDownLatch(1);
	
	@BeforeClass
	public static void init() throws URISyntaxException {
		uri = new URI("tcp://localhost:8888/");
		uriSsl = new URI("ssl://localhost:8888/");
		
		serverChannelWrapper = new TcpServerChannelWrapper(uri.getPort());
		serverChannelWrapper.run();
		serverChannelWrapper.setChannelHandler(new ChannelHandler() {
			
			public void onMessage(ChannelContext context) throws Exception {
				System.out.println(context.getMessage());
				latch.countDown();
			}
			
			public void onError(ChannelContext context) throws Exception {
			}
			
			public void onConnect(ChannelContext context) throws Exception {
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
	public void connect_test() throws ChannelException {
		ClientChannel clientChannel = TcpClient.connect(uri, 100, Channels.pipeline());
		assertNotNull(clientChannel);
	}
	
	@Test
	public void ssl_test() throws ChannelException {
		serverChannelWrapper.ssl();
		ClientChannel clientChannel = TcpClient.connect(uriSsl, 100, Channels.pipeline());
		assertNotNull(clientChannel);
	}
	
	@Test
	public void send_test() throws ChannelException, InterruptedException {
		ClientChannel clientChannel = TcpClient.connect(uri, 100, Channels.pipeline());
		byte[] data = "1234".getBytes();
		clientChannel.send(data, 0, data.length);
		assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
	}
}