package com.taobao.top.link.endpoint;

import java.util.HashMap;

public interface MessageHandler {
	public void onMessage(EndpointContext context) throws Exception;
	public void onMessage(HashMap<String, String> message);
}