package com.taobao.top.link.endpoint;

import java.util.Map;

public interface MessageHandler {
	public void onMessage(EndpointContext context) throws Exception;
	// just got msg that can not reply
	public void onMessage(Map<String, Object> message, Identity messageFrom);
}