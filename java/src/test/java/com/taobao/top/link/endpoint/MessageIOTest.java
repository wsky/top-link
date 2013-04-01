package com.taobao.top.link.endpoint;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
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
		msg.content = new HashMap<String, String>();
		msg.content.put("name1", "中文abc");
		msg.content.put("name2", "abc");

		ByteBuffer buffer = ByteBuffer.allocate(1024);
		MessageIO.writeMessage(buffer, msg);
		buffer.flip();
		Message msg2 = MessageIO.readMessage(buffer);

		assertEquals(msg.messageType, msg2.messageType);
		assertEquals(msg.flag, msg2.flag);
		assertEquals(msg.token, msg2.token);
		assertEquals(msg.statusCode, msg2.statusCode);
		assertEquals(msg.statusPhase, msg2.statusPhase);
		assertEquals(msg.content.size(), msg2.content.size());
		assertEquals(msg.content.get("name1"), msg2.content.get("name1"));
		assertEquals(msg.content.get("name2"), msg2.content.get("name2"));
	}
}
