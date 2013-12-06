package com.taobao.top.link.endpoint;

public interface MessageHandler {
	public void onMessage(EndpointContext context) throws Exception;
	public void onAckMessage(EndpointBaseContext context);
}