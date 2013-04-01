package com.taobao.top.link.endpoint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map.Entry;

import com.taobao.top.link.endpoint.MessageType.HeaderType;

// simple protocol impl
//care about Endian
//https://github.com/wsky/RemotingProtocolParser/issues/3
public class MessageIO {
	public static Message readMessage(ByteBuffer buffer) {
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		Message msg = new Message();
		msg.messageType = buffer.getShort();
		// read kv
		HashMap<String, String> dict = new HashMap<String, String>();
		short headerType = buffer.getShort();
		while (headerType != HeaderType.EndOfHeaders) {
			if (headerType == HeaderType.Custom)
				dict.put(readCountedString(buffer), readCountedString(buffer));
			else if (headerType == HeaderType.StatusCode)
				msg.statusCode = buffer.getInt();
			else if (headerType == HeaderType.StatusPhrase)
				msg.statusPhase = readCountedString(buffer);
			else if (headerType == HeaderType.Flag)
				msg.flag = buffer.getInt();
			else if (headerType == HeaderType.Token)
				msg.token = readCountedString(buffer);
			headerType = buffer.getShort();
		}
		msg.content = dict;

		buffer.order(ByteOrder.BIG_ENDIAN);
		return msg;
	}

	public static void writeMessage(ByteBuffer buffer, Message message) {
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		buffer.putShort(message.messageType);

		if (message.statusCode > 0) {
			buffer.putShort(HeaderType.StatusCode);
			buffer.putInt(message.statusCode);
		}
		if (message.statusPhase != null && message.statusPhase != "") {
			buffer.putShort(HeaderType.StatusPhrase);
			writeCountedString(buffer, message.statusPhase);
		}
		if (message.flag > 0) {
			buffer.putShort(HeaderType.Flag);
			buffer.putInt(message.flag);
		}
		if (message.token != null && message.token != "") {
			buffer.putShort(HeaderType.Token);
			writeCountedString(buffer, message.token);
		}
		if (message.content != null) {
			for (Entry<String, String> i : message.content.entrySet())
				writeCustomHeader(buffer, i.getKey(), i.getValue().toString());
		}
		buffer.putShort(HeaderType.EndOfHeaders);

		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.flip();
	}

	// UTF-8 only
	private static String readCountedString(ByteBuffer buffer)
	{
		int size = buffer.getInt();
		if (size > 0) {
			byte[] data = new byte[size];
			buffer.get(data, 0, data.length);
			return new String(data, Charset.forName("UTF-8"));
		}
		return null;
	}

	private static void writeCountedString(ByteBuffer buffer, String value)
	{
		int strLength = 0;
		if (value != null)
			strLength = value.length();

		if (strLength > 0) {
			byte[] strBytes = value.getBytes(Charset.forName("UTF-8"));
			buffer.putInt(strBytes.length);
			buffer.put(strBytes);
		}
		else
			buffer.putInt(0);
	}

	private static void writeCustomHeader(ByteBuffer buffer, String name, String value)
	{
		buffer.putShort(HeaderType.Custom);
		writeCountedString(buffer, name);
		writeCountedString(buffer, value);
	}
}
