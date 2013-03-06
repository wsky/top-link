package com.taobao.top.link.remoting;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.junit.Test;

public class SerializeTest {
	@SuppressWarnings("unchecked")
	@Test
	public void java_object_stream_test() throws IOException, ClassNotFoundException {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("key", "abc");
		HashMap<String, String> newmap = (HashMap<String, String>) this.process(map);
		assertEquals(map.get("key"), newmap.get("key"));
	}

	@Test
	public void methodReturn_test() throws IOException, ClassNotFoundException {
		MethodReturn methodReturn = new MethodReturn();
		methodReturn.ReturnValue = "hi";
		methodReturn.Exception = new Exception("error");
		methodReturn = (MethodReturn) this.process(methodReturn);
		assertEquals("hi", methodReturn.ReturnValue);
		assertNotNull(methodReturn.Exception);
		assertEquals("error", methodReturn.Exception.getMessage());
	}

	private Object process(Object origin) throws IOException, ClassNotFoundException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(origin);
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bis);
		return ois.readObject();
	}
}
