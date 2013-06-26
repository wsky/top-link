package com.taobao.top.link.endpoint;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import com.taobao.top.link.endpoint.MessageType.HeaderType;
import com.taobao.top.link.endpoint.MessageType.ValueFormat;

// simple protocol impl
// care about Endian
// https://github.com/wsky/RemotingProtocolParser/issues/3
public class MessageIO {

	public static Message readMessage(ByteBuffer buffer) {
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		Message msg = new Message();
		msg.protocolVersion = buffer.get();
		msg.messageType = buffer.get();
		// read kv
		HashMap<String, Object> dict = new HashMap<String, Object>();
		short headerType = buffer.getShort();
		while (headerType != HeaderType.EndOfHeaders) {
			if (headerType == HeaderType.Custom)
				dict.put(readCountedString(buffer), readCustomValue(buffer));
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

		buffer.put((byte) message.protocolVersion);
		buffer.put((byte) message.messageType);

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
			for (Entry<String, Object> i : message.content.entrySet())
				writeCustomHeader(buffer, i.getKey(), i.getValue());
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

	private static void writeCustomHeader(ByteBuffer buffer, String name, Object value)
	{
		buffer.putShort(HeaderType.Custom);
		writeCountedString(buffer, name);
		writeCustomValue(buffer, value);
	}

	private static Object readCustomValue(ByteBuffer buffer) {
		byte format = buffer.get();
		switch (format) {
		case ValueFormat.Void:
			return null;
		case ValueFormat.Byte:
			return buffer.get();
		case ValueFormat.Int16:
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			short value = buffer.getShort();
			buffer.order(ByteOrder.BIG_ENDIAN);
			return value;
		case ValueFormat.Int32:
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			int intValue = buffer.getInt();
			buffer.order(ByteOrder.BIG_ENDIAN);
			return intValue;
		case ValueFormat.Int64:
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			long longValue = buffer.getLong();
			buffer.order(ByteOrder.BIG_ENDIAN);
			return longValue;
		case ValueFormat.Date:
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			long ticks = buffer.getLong();
			buffer.order(ByteOrder.BIG_ENDIAN);
			return new Date(ticks);
		default:
			return readCountedString(buffer);
		}
	}

	private static void writeCustomValue(ByteBuffer buffer, Object value) {
		if (value == null) {
			buffer.put(ValueFormat.Void);
			return;
		}
		Class<?> type = value.getClass();
		if (byte.class.equals(type) || Byte.class.equals(type)) {
			buffer.put(ValueFormat.Byte);
			buffer.put((Byte) value);
		} else if (short.class.equals(type) || Short.class.equals(type)) {
			buffer.put(ValueFormat.Int16);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			buffer.putShort((Short) value);
			buffer.order(ByteOrder.BIG_ENDIAN);
		} else if (int.class.equals(type) || Integer.class.equals(type)) {
			buffer.put(ValueFormat.Int32);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			buffer.putInt((Integer) value);
			buffer.order(ByteOrder.BIG_ENDIAN);
		} else if (long.class.equals(type) || Long.class.equals(type)) {
			buffer.put(ValueFormat.Int64);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			buffer.putLong((Long) value);
			buffer.order(ByteOrder.BIG_ENDIAN);
		} else if (Date.class.equals(type)) {
			buffer.put(ValueFormat.Date);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			buffer.putLong(((Date) value).getTime());
			buffer.order(ByteOrder.BIG_ENDIAN);
		} else {
			buffer.put(ValueFormat.CountedString);
			writeCountedString(buffer, (String) value);
		}
	}
}
