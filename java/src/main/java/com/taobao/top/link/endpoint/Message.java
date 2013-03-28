package com.taobao.top.link.endpoint;

import java.util.HashMap;

// just simple version
public class Message {
	public short messageType;
	public int flag;
	public String token;
	// SEND
	public HashMap<String, String> content;
	// CONNECT
	public Identity identity;
	// CONNECTACK
	public int statusCode;
	public String statusPhase;
}