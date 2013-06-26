package com.taobao.top.link.endpoint;

import java.util.Map;

// just simple version
public class Message {
	public short protocolVersion = 1;
	public short messageType;

	public int statusCode;
	public String statusPhase;
	public int flag;
	public String token;

	public Map<String, Object> content;
}