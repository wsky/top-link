package com.taobao.top.link.endpoint;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;

import org.junit.Test;

public class MessageIOTest {
	@Test
	public void read_write_test() {
		Message msg = new Message();
		msg.messageType = MessageType.SEND;
		msg.flag = 10;
		msg.token = "abc";
		msg.statusCode = 100;
		msg.statusPhase = "abcd";
		msg.content = new HashMap<String, Object>();
		msg.content.put("name1", "中文abc");
		msg.content.put("name2", "abc");

		msg.content.put("void", null);
		msg.content.put("byte", (byte) 1);
		msg.content.put("int16", (short) 100);
		msg.content.put("int32", (int) 100);
		msg.content.put("int64", (long) 100);
		msg.content.put("date", new Date());

		ByteBuffer buffer = ByteBuffer.allocate(1024);
		MessageIO.writeMessage(buffer, msg);
		// buffer.flip();//flip in write
		Message msg2 = MessageIO.readMessage(buffer);

		assertEquals(msg.messageType, msg2.messageType);
		assertEquals(msg.flag, msg2.flag);
		assertEquals(msg.token, msg2.token);
		assertEquals(msg.statusCode, msg2.statusCode);
		assertEquals(msg.statusPhase, msg2.statusPhase);
		assertEquals(msg.content.size(), msg2.content.size());
		assertEquals(msg.content.get("name1"), msg2.content.get("name1"));
		assertEquals(msg.content.get("name2"), msg2.content.get("name2"));

		assertEquals(msg.content.get("void"), msg2.content.get("void"));
		assertEquals(msg.content.get("byte"), msg2.content.get("byte"));
		assertEquals(msg.content.get("int16"), msg2.content.get("int16"));
		assertEquals(msg.content.get("int32"), msg2.content.get("int32"));
		assertEquals(msg.content.get("int64"), msg2.content.get("int64"));
		assertEquals(msg.content.get("date"), msg2.content.get("date"));
	}
}
