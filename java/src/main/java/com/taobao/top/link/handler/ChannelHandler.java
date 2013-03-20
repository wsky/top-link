package com.taobao.top.link.handler;

import java.nio.ByteBuffer;

import com.taobao.top.link.EndpointContext;

public interface ChannelHandler {
	public void onReceive(ByteBuffer dataBuffer, EndpointContext context);
	public void onException(Throwable exception);
}
