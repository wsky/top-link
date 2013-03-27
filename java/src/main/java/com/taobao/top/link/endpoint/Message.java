package com.taobao.top.link.endpoint;

import java.nio.ByteBuffer;

public class Message {
	public short messageType;
	public int flag;
	public int contentLength;
	public ByteBuffer content;
	
	public int statusCode;
	public String statusPhase;
}
