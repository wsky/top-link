package com.taobao.top.link.remoting;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.junit.Test;

public class SerializeTest {
	@Test
	public void java_object_stream_test() throws IOException, ClassNotFoundException {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("key", "abc");

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(map);

		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bis);
		@SuppressWarnings("unchecked")
		HashMap<String, String> newmap = (HashMap<String, String>) ois.readObject();

		assertEquals(map.get("key"), newmap.get("key"));
	}
}
