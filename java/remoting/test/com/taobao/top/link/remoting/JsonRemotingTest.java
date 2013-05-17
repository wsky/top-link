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
		HashMap<String, String> map1 = new HashMap<String, String>();
		map1.put("k", "k");
		HashMap<String, String> map2 = new HashMap<String, String>();
		map2.put("k", "k");
		Entity entity = new Entity();
		entity.setString("abc中文");
		entity.setLong(10);
		entity.setDate(new Date());
		entity.setMap(map2);
		entity.setArray(new String[] { "abc" });

		Entity ret = serialInterface.echo("string",
				(byte) 0,
				(double) 0,
				(float) 0,
				(int) 0,
				(long) 0,
				(short) 0,
				new Date(),
				map1,
				entity,
				new String[] { "abc" });

		assertEquals(entity.getString(), ret.getString());
		assertEquals(entity.getLong(), ret.getLong());
		assertEquals(entity.getDate(), ret.getDate());
		assertEquals(entity.getMap().size(), ret.getMap().size());
		assertEquals(entity.getMap().get("k"), ret.getMap().get("k"));
		assertEquals(entity.getArray()[0], ret.getArray()[0]);
	}

	// @Test
	public void simply_perf_test() {
		Entity entity = new Entity();
		entity.setString("hello1234567890123456789123456789hello1234567890123456789123456789");

		int total = 100000;
		long begin = System.currentTimeMillis();
		for (int i = 0; i < total; i++) {
			serialInterface.echo("string",
					(byte) 0,
					(double) 0,
					(float) 0,
					(int) 0,
					(long) 0,
					(short) 0,
					new Date(),
					null,
					entity,
					null);
		}
		long cost = System.currentTimeMillis() - begin;
		System.out.println(String.format(
				"total:%s, cost:%sms, tps:%scall/s, time:%sms", total, cost,
				((float) total / (float) cost) * 1000,
				(float) cost / (float) total));

		// java build-in
		// total:100000, cost:110834ms, tps:902.25024call/s, time:1.10834ms
		// json
		// total:100000, cost:16781ms, tps:5959.12call/s, time:0.16781ms
	}
}