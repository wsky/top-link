package com.taobao.top.link.endpoint;

import java.nio.ByteBuffer;

//care about Endian
//https://github.com/wsky/RemotingProtocolParser/issues/3
public class MessageIO {
	public static Message readMessage(ByteBuffer buffer) {
		return new Message();
	}

	public static void writeMessage(ByteBuffer buffer, Message message) {

	}
}
