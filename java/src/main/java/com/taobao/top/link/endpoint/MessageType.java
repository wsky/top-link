package com.taobao.top.link.endpoint;

public class MessageType {
	public static final short CONNECT = 0;
	public static final short CONNECTACK = 1;

	public static final short SEND = 2;
	public static final short SENDACK = 3;

	public class HeaderType {
		public final static short EndOfHeaders = 0;
		public final static short Custom = 1;
		public final static short StatusCode = 2;
		public final static short StatusPhrase = 3;
		public final static short Flag = 4;
		public final static short Token = 5;
	}
}