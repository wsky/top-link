package com.taobao.top.link.endpoint;

import java.util.HashMap;

// just simple version
public class Message {
	public short messageType;
	
	public int statusCode;
	public String statusPhase;
	public int flag;
	public String token;
	
	public HashMap<String, String> content;
}