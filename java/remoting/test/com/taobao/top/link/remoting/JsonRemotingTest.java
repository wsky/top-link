package com.taobao.top.link.remoting;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.link.DefaultLoggerFactory;
import com.taobao.top.link.channel.ClientChannelPooledSelector;
import com.taobao.top.link.channel.websocket.WebSocketServerChannel;

public class JsonRemotingTest {
	private static URI uri;
	private static WebSocketServerChannel serverChannel;
	private static DynamicProxy proxy;
	private static SerialInterface serialInterface;

	@BeforeClass
	public static void init() throws URISyntaxException {
		uri = new URI("ws://localhost:8888/json");

		CrossLanguageJsonSerializer serializer = new CrossLanguageJsonSerializer();

		DefaultRemotingServerChannelHandler serverHandler = new DefaultRemotingServerChannelHandler();
		serverHandler.setSerializer(serializer);
		serverHandler.addProcessor("json", new SerialService());
		serverChannel = new WebSocketServerChannel(uri.getPort());
		serverChannel.setChannelHandler(serverHandler);
		serverChannel.run();

		RemotingClientChannelHandler clientHandler = new RemotingClientChannelHandler(DefaultLoggerFactory.getDefault(), new AtomicInteger(0));
		clientHandler.setSerializer(serializer);
		proxy = new DynamicProxy(uri, new ClientChannelPooledSelector(), clientHandler);
		serialInterface = (SerialInterface) proxy.create(SerialInterface.class, uri);
	}

	@AfterClass
	public static void clear() {
		serverChannel.stop();
	}

	@Test
	public void invoke_test() throws FormatterException, RemotingException {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("k", "k");
		Entity entity = new Entity();
		entity.String = "abc中文";
		entity.Long = 10;
		entity.Date = new Date();
		entity.Map = map;
		Entity ret = serialInterface.echo("string",
				(byte) 0,
				(double) 0,
				(float) 0,
				(int) 0,
				(long) 0,
				(short) 0,
				new Date(),
				map,
				entity);
		assertEquals(entity.String, ret.String);
		assertEquals(entity.Long, ret.Long);
		assertEquals(entity.Date, ret.Date);
		assertEquals(entity.Map.size(), ret.Map.size());
		assertEquals(entity.Map.get("k"), ret.Map.get("k"));
	}
}